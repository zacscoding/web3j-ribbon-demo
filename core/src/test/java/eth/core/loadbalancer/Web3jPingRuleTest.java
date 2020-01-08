package eth.core.loadbalancer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthSyncing;

import com.fasterxml.jackson.databind.ObjectMapper;

import eth.core.EthNode;
import eth.core.loadbalancer.PingRule.ConnectedOnly;
import eth.core.loadbalancer.PingRule.Synchronized;

public class Web3jPingRuleTest {

    EthNode node = mock(EthNode.class);
    Web3jService web3jService = mock(Web3jService.class);
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    EthSyncing syncingResult;
    EthSyncing synchronizedResult;

    @BeforeEach
    public void setUp() throws Exception {
        when(node.getWeb3jService()).thenReturn(web3jService);
        syncingResult = objectMapper.readValue(
                "{\n"
                + "  \"id\":1,\n"
                + "  \"jsonrpc\": \"2.0\",\n"
                + "  \"result\": {\n"
                + "  \"startingBlock\": \"0x380\",\n"
                + "  \"currentBlock\": \"0x383\",\n"
                + "  \"highestBlock\": \"0x389\"\n"
                + "  }\n"
                + "}", EthSyncing.class);

        synchronizedResult = objectMapper.readValue(
                "{\n"
                + "  \"id\":1,\n"
                + "  \"jsonrpc\": \"2.0\",\n"
                + "  \"result\": false\n"
                + "}", EthSyncing.class);

    }

    @Test
    @DisplayName("success - connected only ping rule")
    public void testConnectedOnlyRule() throws Exception {
        // given
        Web3jPing ping = Web3jPing.of(ConnectedOnly.INSTANCE);
        when(web3jService.send(any(Request.class), eq(EthSyncing.class)))
                .thenReturn(synchronizedResult);

        // when then
        assertThat(ping.isAlive(node)).isTrue();
    }

    @Test
    @DisplayName("fail - connected only ping rule")
    public void testConnectedOnlyRuleFailIfErrorResponse() throws Exception {
        // given
        Web3jPing ping = Web3jPing.of(ConnectedOnly.INSTANCE);
        when(web3jService.send(any(Request.class), eq(EthSyncing.class)))
                .thenThrow(new IOException("Forced exception"));

        // when then
        assertThat(ping.isAlive(node)).isFalse();
    }

    @Test
    @DisplayName("syncing with threshold")
    public void testSyncingRule() throws Exception {
        // given
        when(web3jService.send(any(Request.class), eq(EthSyncing.class)))
                .thenReturn(syncingResult);
        final long[] failuresDiff = { 5L, 4L, 3L };
        final long[] successesDiff = { 6L, 7L, 8L, 9L };

        // when then
        for (long failureDiff : failuresDiff) {
            Web3jPing ping = Web3jPing.of(PingRule.Syncing.of(BigInteger.valueOf(failureDiff)));
            assertThat(ping.isAlive(node)).isFalse();
        }

        for (long successDiff : successesDiff) {
            Web3jPing ping = Web3jPing.of(PingRule.Syncing.of(BigInteger.valueOf(successDiff)));
            assertThat(ping.isAlive(node)).isTrue();
        }
    }

    @Test
    @DisplayName("synchronized")
    public void testSynchronizedRule() throws Exception {
        // given
        Web3jPing ping = Web3jPing.of(Synchronized.INSTANCE);
        when(web3jService.send(any(Request.class), eq(EthSyncing.class)))
                .thenReturn(synchronizedResult);

        // when then
        assertThat(ping.isAlive(node)).isTrue();
    }

    @Test
    @DisplayName("fail - synchronized rule")
    public void testSynchronizedRuleFailIfSyncing() throws Exception {
        // given
        Web3jPing ping = Web3jPing.of(Synchronized.INSTANCE);
        when(web3jService.send(any(Request.class), eq(EthSyncing.class)))
                .thenReturn(syncingResult);

        // when then
        assertThat(ping.isAlive(node)).isFalse();
    }
}
