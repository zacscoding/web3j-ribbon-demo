package eth.core.loadbalancer;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;

import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.utils.Numeric;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;

import eth.core.EthNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Web3jPing implements IPing {

    private PingRule pingRule;

    /**
     * Returns a {@link PingRule}
     */
    public static Web3jPing of(PingRule pingRule) {
        return new Web3jPing(pingRule);
    }

    private Web3jPing(PingRule pingRule) {
        this.pingRule = checkNotNull(pingRule, "pingRule");
    }

    @Override
    public boolean isAlive(Server server) {
        if (!(server instanceof EthNode)) {
            throw new IllegalStateException("Cannot cast to Server to EthNode. server : "
                                            + (server == null ? null : server.getClass().getName()));
        }

        try {
            final EthNode ethNode = (EthNode) server;
            final EthSyncing.Result result = getSyncingResult(ethNode);

            // 1) connected only
            if (pingRule.isConnectedOnly()) {
                return true;
            }

            // 2) completed synchronize
            if (!result.isSyncing()) {
                return true;
            }

            // 3) syncing now

            // 3-1) rule : synchronized
            if (pingRule.isSynchronized()) {
                return false;
            }

            // 3-2) rule : syncing -> check threshold
            if (!(result instanceof EthSyncing.Syncing)) {
                logger.warn("Received unknown syncing result type {}.", result.getClass().getName());
                return false;
            }

            final EthSyncing.Syncing syncing = (EthSyncing.Syncing) result;
            final BigInteger highestBlockNumber =
                    new BigInteger(Numeric.hexStringToByteArray(syncing.getHighestBlock()));
            final BigInteger currentBlockNumber =
                    new BigInteger(Numeric.hexStringToByteArray(syncing.getCurrentBlock()));

            final BigInteger diff = highestBlockNumber.subtract(currentBlockNumber);

            return diff.compareTo(pingRule.getSyncingThreshold()) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private EthSyncing.Result getSyncingResult(EthNode ethNode) throws Exception {
        final Web3j web3j = Web3j.build(ethNode.getWeb3jService());
        try {
            return web3j.ethSyncing().send().getResult();
        } catch (WebsocketNotConnectedException e) {
            final Web3jService web3jService = ethNode.getWeb3jService();

            // throw exception if not web socket service
            if (web3jService instanceof WebSocketService) {
                // if use websocket service, then try to reconnect
                ((WebSocketService) web3jService).connect();
                return web3j.ethSyncing().send().getResult();
            }

            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
