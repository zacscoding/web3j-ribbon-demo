package example.eth.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import eth.core.EthNode;
import eth.core.RpcServiceFactory;
import eth.core.loadbalancer.Web3jServerList;
import example.eth.domain.EthChainNode;
import example.eth.repository.EthChainNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class JpaWeb3jServerList implements Web3jServerList {

    private final EthChainNodeRepository nodeRepository;
    private final RpcServiceFactory rpcServiceFactory;

    private final Map<Long, EthNode> nodesMap = new HashMap<>();

    @Override
    public List<EthNode> getInitialListOfServers() {
        List<EthChainNode> nodeEntities = nodeRepository.findAllWithChain();
        List<EthNode> ret = new ArrayList<>(nodeEntities.size());

        for (EthChainNode nodeEntity : nodeEntities) {
            EthNode ethNode = convertToNode(nodeEntity);

            nodesMap.put(nodeEntity.getId(), ethNode);
            ret.add(ethNode);
        }

        return ret;
    }

    @Override
    public List<EthNode> getUpdatedListOfServers() {
        Set<Long> nodeIdSet = nodesMap.keySet();

        List<EthChainNode> nodeEntities = nodeRepository.findAllWithChain();
        List<EthNode> ret = new ArrayList<>(nodeEntities.size());

        for (EthChainNode nodeEntity : nodeEntities) {
            EthNode node;

            // already exist
            if (nodeIdSet.remove(nodeEntity.getId())) {
                node = nodesMap.get(nodeEntity.getId());
            } else { // new one
                node = convertToNode(nodeEntity);
                nodesMap.put(nodeEntity.getId(), node);
                logger.debug("A new eth node : {}", node);
            }

            ret.add(node);
        }

        // removed a node
        for (Long removedId : nodeIdSet) {
            EthNode remove = nodesMap.remove(removedId);
            logger.debug("Removed a eth node : {}", remove);
        }

        return ret;
    }

    private EthNode convertToNode(EthChainNode nodeEntity) {
        return EthNode.of(nodeEntity.getChain().getChainId(),
                          nodeEntity.getRpcProtocolType().getScheme(),
                          nodeEntity.getRpcHost(),
                          nodeEntity.getRpcPort(),
                          rpcServiceFactory);
    }
}
