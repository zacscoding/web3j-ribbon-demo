package eth.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

public class DefaultRpcServiceFactoryTest {

    RpcServiceFactory rpcServiceFactory = new DefaultRpcServiceFactory();

    @Test
    @DisplayName("creation of http service")
    public void testCreateHttpService() {
        // when
        Web3jService web3jService = rpcServiceFactory.createWeb3jService("http", "192.168.1.1", 8540);

        // then
        assertThat(web3jService instanceof HttpService).isTrue();

        // when
        web3jService = rpcServiceFactory.createWeb3jService("https", "ethbalancer", 8540);

        // then
        assertThat(web3jService instanceof HttpService).isTrue();
    }

    @Test
    @DisplayName("creation of websocket service")
    public void testCreateWebSocketService() {
        // when
        Web3jService web3jService = rpcServiceFactory.createWeb3jService("ws", "192.168.1.1", 9540);

        // then
        assertThat(web3jService instanceof WebSocketService).isTrue();
    }
}
