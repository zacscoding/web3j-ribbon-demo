package eth.core.loadbalancer;

import org.web3j.protocol.Web3jService;

import com.netflix.loadbalancer.ServerList;

import eth.core.EthNode;

/**
 * Extended {@link ServerList} for list of {@link Web3jService}
 */
public interface Web3jServerList extends ServerList<EthNode> {
}
