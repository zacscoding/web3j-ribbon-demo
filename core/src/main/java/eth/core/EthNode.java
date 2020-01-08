package eth.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.web3j.protocol.Web3jService;

import com.netflix.loadbalancer.Server;

/**
 * Ethereum node extends {@link Server}
 */
public class EthNode extends Server {

    private final String chainId;
    private final String scheme;
    private final String host;
    private final int port;
    private final String rpcUrl;
    private final Map<String, Object> metadata;
    private Web3jService web3jService;

    /**
     * Create a new {@link EthNode} with {@link DefaultRpcServiceFactory}, empty metadata
     *
     * @param chainId : chain id
     * @param scheme : protocol (http | https | ws)
     * @param host : target host
     * @param port : target port
     */
    public static EthNode of(String chainId, String scheme, String host, int port) {
        return of(chainId, scheme, host, port, new DefaultRpcServiceFactory());
    }

    /**
     * Create a new {@link EthNode} with empty metadata
     *
     * @param chainId : chain id
     * @param scheme : protocol (http | https | ws)
     * @param host : target host
     * @param port : target port
     * @param rpcServiceFactory : {@link Web3jService} creation factory
     */
    public static EthNode of(String chainId, String scheme, String host, int port,
                             RpcServiceFactory rpcServiceFactory) {
        return new EthNode(chainId, scheme, host, port, rpcServiceFactory, new HashMap<>());
    }

    /**
     * Create a new {@link EthNode} given args
     *
     * @param chainId : chain id
     * @param scheme : protocol (http | https | ws)
     * @param host : target host
     * @param port : target port
     * @param rpcServiceFactory : {@link Web3jService} creation factory
     * @param metadata : this node's metadata
     */
    public static EthNode of(String chainId, String scheme, String host, int port,
                             RpcServiceFactory rpcServiceFactory, Map<String, Object> metadata) {

        return new EthNode(chainId, scheme, host, port, new DefaultRpcServiceFactory(), metadata);
    }

    private EthNode(String chainId, String scheme, String host, int port, RpcServiceFactory rpcServiceFactory,
                    Map<String, Object> metadata) {

        super(checkNotNull(scheme, "scheme"),
              checkNotNull(host, "host"), port);

        this.chainId = checkNotNull(chainId, "chainId");
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.rpcUrl = String.format("%s://%s:%d", scheme, host, port);
        this.metadata = metadata;
        this.web3jService = rpcServiceFactory.createWeb3jService(scheme, host, port);
    }

    /**
     * Returns a metadata of this node
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Returns a rpc url {scheme}://{host}:{port}
     */
    public String getRpcUrl() {
        return rpcUrl;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    /**
     * Returns a Web3jService
     */
    public Web3jService getWeb3jService() {
        return web3jService;
    }

    /**
     * Returns a chain id
     */
    public String getChainId() {
        return chainId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof EthNode)) { return false; }
        EthNode ethNode = (EthNode) o;
        return Objects.equals(getRpcUrl(), ethNode.getRpcUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRpcUrl());
    }
}
