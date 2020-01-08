package eth.core.loadbalancer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;

import eth.core.EthNode;
import lombok.extern.slf4j.Slf4j;

/**
 * Extended {@link RoundRobinRule} for multiple chain
 */
@Slf4j
public class MultiChainRoundRobinRule extends AbstractLoadBalancerRule {

    private ConcurrentHashMap<String, AtomicInteger> counterGroup;
    private static final boolean AVAILABLE_ONLY_SERVERS = true;
    private static final boolean ALL_SERVERS = false;

    public MultiChainRoundRobinRule() {
        this.counterGroup = new ConcurrentHashMap<>();
    }

    public MultiChainRoundRobinRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        // empty
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    public Server choose(ILoadBalancer lb, final Object key) {
        if (lb == null) {
            logger.warn("no load balancer");
            return null;
        }

        if (key == null) {
            logger.warn("no load balancer key");
            return null;
        }

        final String chainId = key.toString();
        final AtomicInteger nextServerCyclicCounter = getNextServerCyclicCounter(chainId);
        Server server = null;
        int count = 0;

        final Predicate<Server> filter = s -> {
            if (!(s instanceof EthNode)) {
                return false;
            }

            return chainId.equals(((EthNode) s).getChainId());
        };

        while (count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers()
                                              .stream()
                                              .filter(filter)
                                              .collect(Collectors.toList());

            List<Server> allServers = lb.getAllServers()
                                        .stream()
                                        .filter(filter)
                                        .collect(Collectors.toList());

            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if (upCount == 0) {
                String instanceIds = allServers.stream()
                                               .map(s -> ((EthNode) s).getRpcUrl())
                                               .collect(Collectors.joining(","));

                logger.warn("No available up servers from load balancer. instances : {}", instanceIds);
                return null;
            }

            if (serverCount == 0) {
                logger.warn("No available servers from load balancer. i.e serverCount is 0");
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(nextServerCyclicCounter, serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }
        }

        if (count >= 10) {
            logger.warn("No available alive servers after 10 tries from load balancer: {}", lb);
        }

        return null;
    }

    /**
     * Copy source code from {@link RoundRobinRule}
     *
     * Inspired by the implementation of {@link AtomicInteger#incrementAndGet()}.
     *
     * @param modulo The modulo to bound the value of the counter.
     * @return The next value.
     */
    private int incrementAndGetModulo(AtomicInteger nextServerCyclicCounter, int modulo) {
        while (true) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) { return next; }
        }
    }

    /**
     * Get or create a nextNodeCyclicCounter given chain id
     *
     * @param chainId : ethereum chain id
     */
    private AtomicInteger getNextServerCyclicCounter(String chainId) {
        return counterGroup.computeIfAbsent(chainId, (key) -> new AtomicInteger(0));
    }
}