package example.eth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import example.eth.domain.EthChain;

@Repository
public interface EthChainRepository extends JpaRepository<EthChain, Long> {

    Optional<EthChain> findByChainId(@Param("chainId") String chainId);

    @EntityGraph(attributePaths = "nodes")
    Optional<EthChain> findWithNodesByChainId(@Param("chainId") String chainId);
}
