package eth.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

/**
 * Default rpc service creation factory
 */
public class DefaultRpcServiceFactory implements RpcServiceFactory {

    @Override
    public Web3jService createWeb3jService(String scheme, String host, int port) {
        checkNotNull(scheme, "scheme");
        checkNotNull(host, "host");
        checkArgument(port > 0, "port must be greater than 0");

        final String rpcUrl = String.format("%s://%s:%d", scheme, host, port);

        if ("ws".equals(scheme)) {
            return new WebSocketService(rpcUrl, false);
        }

        if ("http".equals(scheme) || "https".equals(scheme)) {
            return new HttpService(rpcUrl, false);
        }

        throw new UnsupportedOperationException("Not support scheme : " + scheme);
    }
}
