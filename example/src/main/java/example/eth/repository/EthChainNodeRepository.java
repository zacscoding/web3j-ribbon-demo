package example.eth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import example.eth.domain.EthChainNode;

@Repository
public interface EthChainNodeRepository extends JpaRepository<EthChainNode, Long> {

    @Query("select cn from EthChainNode cn join fetch cn.chain c order by cn.id asc")
    List<EthChainNode> findAllWithChain();
}
