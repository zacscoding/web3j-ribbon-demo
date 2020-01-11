package example.eth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import com.netflix.loadbalancer.PollingServerListUpdater;
import com.netflix.loadbalancer.ServerListUpdater;

import eth.core.DefaultRpcServiceFactory;
import eth.core.EthNode;
import eth.core.RpcServiceFactory;
import eth.core.loadbalancer.MultiChainLoadBalancer;
import eth.core.loadbalancer.MultiChainRoundRobinRule;
import eth.core.loadbalancer.PingRule.ConnectedOnly;
import eth.core.loadbalancer.Web3jPing;
import example.eth.repository.EthChainNodeRepository;
import example.eth.support.JpaWeb3jServerList;

@Configuration
public class EthConfiguration {

    @Bean
    public RpcServiceFactory rpcServiceFactory() {
        return new DefaultRpcServiceFactory();
    }

    @Bean
    public IPing ethIPing() {
        return Web3jPing.of(ConnectedOnly.INSTANCE);
    }

    @Bean
    public IRule ethIRule() {
        return new MultiChainRoundRobinRule();
    }

    @Bean
    public ServerListUpdater ethServiceListUpdater() {
        return new PollingServerListUpdater(5000L, 10000L);
    }

    @Bean
    public JpaWeb3jServerList ethServerList(EthChainNodeRepository nodeRepository) {
        return new JpaWeb3jServerList(nodeRepository, rpcServiceFactory());
    }

    @Bean
    public AbstractLoadBalancer ethLoadBalancer(EthChainNodeRepository nodeRepository) {
        LoadBalancerBuilder<EthNode> builder = LoadBalancerBuilder.newBuilder();

        return builder.withPing(ethIPing())
                      .withRule(ethIRule())
                      .withDynamicServerList(ethServerList(nodeRepository))
                      .withServerListUpdater(ethServiceListUpdater())
                      .buildDynamicServerListLoadBalancerWithUpdater();
    }

    @Bean
    public MultiChainLoadBalancer multiChainLoadBalancer(EthChainNodeRepository nodeRepository) {
        return new MultiChainLoadBalancer(ethLoadBalancer(nodeRepository));
    }

}
