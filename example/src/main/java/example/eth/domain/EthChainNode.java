package example.eth.domain;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ethereum node entity
 */
@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class EthChainNode {

    @Id
    @GeneratedValue
    @Column(name = "node_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "chain_id")
    private EthChain chain;

    @Column(nullable = false)
    private String rpcHost;

    private int rpcPort;

    @Enumerated(EnumType.STRING)
    private RpcProtocolType rpcProtocolType;

    /**
     * Returns a new {@link EthChainNode} given args
     *
     * @param name : name of a node
     * @param rpcHost : host of rpc
     * @param rpcPort : port of rpc
     * @param rpcProtocolType : protocol type of rpc
     */
    public static EthChainNode createChainNode(String name, RpcProtocolType rpcProtocolType,
                                               String rpcHost, int rpcPort) {

        EthChainNode node = new EthChainNode();

        node.setName(name);
        node.setRpcProtocolType(rpcProtocolType);
        node.setRpcHost(rpcHost);
        node.setRpcPort(rpcPort);

        return node;
    }
}
