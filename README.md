# Ethereum load balancer with Web3j and Ribbon
; this is a demo project for load balanced ethereum client  
i.e load balance from ribbon and ethererum client from web3j.  

- <a href="#getting-started">Getting started</a>
- <a href="#ribbon-component">Ribbon component</a>  
- <a href="#example">Example with spring boot + jpa</a>

---  

<div id="getting-started"></div>

## Ribbon component

todo : with docker-compose  



---  

<div id="ribbon-component"></div>  

## Ribbon component

### `com.netflix.loadbalancer.Server`  
=> `eth.core.EthNode` (<a href="core/src/main/java/eth/core/EthNode.java">Source code</a>)  
Override `Server`'s methods and maintain `Web3jService` in member field  

> `eth.core.EthNode`

```java
package eth.core;  

...

public class EthNode extends Server {

    private final String chainId;
    private final String scheme;
    private final String host;
    private final int port;
    private final String rpcUrl;
    private final Map<String, Object> metadata;
    private Web3jService web3jService;
    
    ...
}
```

### `com.netflix.loadbalancer.IPing`  
=> `eth.core.loadbalancer.Web3jPing` (<a href="core/src/main/java/eth/core/loadbalancer/Web3jPing.java">Source code</a>)  
Override `IPing`'s isAlive method by using `EthNode`'s `Web3jService`  

> `eth.core.loadbalancer.Web3jPing`

```java
package eth.core.loadbalancer;  

...

@Slf4j
public class Web3jPing implements IPing {

    ...

    @Override
    public boolean isAlive(Server server) {
        boolean alive = isAliveInternal(server);

        if (logger.isTraceEnabled()) {
            logger.trace("Ping {} result >> {}", server, alive);
        }

        return alive;
    }
    
    ...
    
    /**
     * Returns a {@link EthSyncing}'s result
     * 
     * if use {@link WebSocketService} and not connected, then try to connect again
     */
    private EthSyncing.Result getSyncingResult(EthNode ethNode) throws Exception {
        final Web3j web3j = Web3j.build(ethNode.getWeb3jService());
        try {
            return web3j.ethSyncing().send().getResult();
        } catch (WebsocketNotConnectedException e) {
            final Web3jService web3jService = ethNode.getWeb3jService();

            if (web3jService instanceof WebSocketService) {
                // if use websocket service, then try to reconnect
                ((WebSocketService) web3jService).connect();
                return web3j.ethSyncing().send().getResult();
            }

            // throw exception if not web socket service
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
```    

### `com.netflix.loadbalancer.IRule`  
=> `eth.core.loadbalancer.MultiChainRoundRobinRule` (<a href="core/src/main/java/eth/core/loadbalancer/MultiChainRoundRobinRule.java">Source code</a>)  
Override `IRule`'s choose methods to maintain multiple nextServerCyclicCounter  

> `eth.core.loadbalancer.MultiChainRoundRobinRule`  

```java
package eth.core.loadbalancer;

...

public class MultiChainRoundRobinRule extends AbstractLoadBalancerRule {
    private ConcurrentHashMap<String, AtomicInteger> counterGroup;
    ...
    
    public Server choose(ILoadBalancer lb, final Object key) {
        ...

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

            ...

            int nextServerIndex = incrementAndGetModulo(nextServerCyclicCounter, serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return server;
            }
        }

        if (count >= 10) {
            logger.warn("No available alive servers after 10 tries from load balancer: {}", lb);
        }

        return null;
    }

    private int incrementAndGetModulo(AtomicInteger nextServerCyclicCounter, int modulo) {
        while (true) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) { return next; }
        }
    }

    private AtomicInteger getNextServerCyclicCounter(String chainId) {
        return counterGroup.computeIfAbsent(chainId, (key) -> new AtomicInteger(0));
    }
}   
```

---  

<div id="example"></div>  

## Example with spring boot + jpa  
