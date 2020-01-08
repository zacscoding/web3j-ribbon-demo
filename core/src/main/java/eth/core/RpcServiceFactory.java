package eth.core;

import org.web3j.protocol.Web3jService;

/**
 * {@link Web3jService} creation factory
 */
@FunctionalInterface
public interface RpcServiceFactory {

    /**
     * Create a {@link Web3jService} given schema,host, port
     *
     * Support only websocket and http protocol.
     */
    Web3jService createWeb3jService(String scheme, String host, int port);
}