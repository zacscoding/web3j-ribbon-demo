package eth.core.loadbalancer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import org.web3j.protocol.Web3jService;

import com.google.common.reflect.Reflection;
import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.Server;

import eth.core.EthNode;

/**
 * {@link Web3jService} load balancer based on multiple ethereum chain
 */
public class MultiChainLoadBalancer {

    private static final int RETRY_COUNT = 3;
    private final AbstractLoadBalancer loadBalancer;
    private final ConcurrentHashMap<String, Web3jService> web3jServiceMap;

    public MultiChainLoadBalancer(AbstractLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        web3jServiceMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns a new load balanced {@link Web3jService} given chain id
     *
     * @param chainId : chain id
     */
    public Web3jService getWeb3jService(final String chainId) {
        return web3jServiceMap.computeIfAbsent(chainId, this::createWeb3jProxy);
    }

    private Web3jService createWeb3jProxy(final String chainId) {
        return Reflection.newProxy(Web3jService.class, (proxy, method, args) -> {
            Throwable exception = null;

            for (int i = 0; i < RETRY_COUNT; i++) {
                try {
                    Server server = loadBalancer.chooseServer(chainId);

                    if (server == null) {
                        if (exception == null) {
                            exception = new IOException("No avaiable web3j service");
                        }

                        continue;
                    }

                    return method.invoke(((EthNode) server).getWeb3jService(), args);
                } catch (InvocationTargetException e) {
                    exception = e.getTargetException() != null ?
                                e.getTargetException() : new IOException("unknown exception");
                } catch (Exception e) {
                    exception = new IOException("unknown exception");
                }
            }

            throw exception;
        });
    }
}
