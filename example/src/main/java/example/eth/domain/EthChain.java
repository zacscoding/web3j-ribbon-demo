package example.eth.domain;

import static java.util.Objects.requireNonNull;
import static javax.persistence.CascadeType.ALL;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Ethereum chain entity
 */
@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
public class EthChain {

    @Id
    @GeneratedValue
    @Column(name = "chain_id")
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String chainId;

    private long averageBlockTime;

    @OneToMany(mappedBy = "chain", cascade = ALL)
    private List<EthChainNode> nodes = new ArrayList<>();

    /**
     * Returns a {@link EthChain} given name and average block time
     *
     * @param name : name of a chain
     * @param averageBlockTime : average block time (milliseconds)
     */
    public static EthChain createEthChain(String name, String chainId, long averageBlockTime) {
        EthChain chain = new EthChain();

        chain.setName(name);
        chain.setChainId(chainId);
        chain.setAverageBlockTime(averageBlockTime);

        return chain;
    }

    /**
     * Adds a {@link EthChainNode} into this chain
     */
    public EthChain addNode(EthChainNode node) {
        requireNonNull(node, "node");

        nodes.add(node);
        node.setChain(this);

        return this;
    }
}
