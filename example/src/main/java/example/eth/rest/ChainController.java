package example.eth.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.eth.domain.EthChain;
import example.eth.domain.EthChainNode;
import example.eth.dto.EthChainDto;
import example.eth.dto.EthChainNodeDto;
import example.eth.repository.EthChainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChainController {

    private final EthChainRepository chainRepository;

    // ====== chain
    @PostMapping("/chain")
    @Transactional
    public ResponseEntity saveChain(@Valid @RequestBody EthChainDto chainDto) {
        EthChain chain = EthChain.createEthChain(chainDto.getName(), chainDto.getChainId(),
                                                 chainDto.getAverageBlockTime());

        chain = chainRepository.save(chain);

        return ResponseEntity.ok(chain.getId());
    }

    @PostMapping("/chain/{chainId}/node")
    @Transactional
    public ResponseEntity saveNode(@PathVariable("chainId") String chainId,
                                   @Valid @RequestBody EthChainNodeDto nodeDto) {

        Optional<EthChain> chainOptional = chainRepository.findByChainId(chainId);

        if (!chainOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        EthChain chain = chainOptional.get();

        EthChainNode node = EthChainNode.createChainNode(nodeDto.getName(), nodeDto.getRpcProtocolType(),
                                                         nodeDto.getHost(), nodeDto.getRpcPort());
        chain.addNode(node);

        chainRepository.save(chain);

        return ResponseEntity.ok(node.getId());
    }

    @GetMapping("/chain/{chainId}")
    public ResponseEntity<EthChainDto> getChain(@PathVariable("chainId") String chainId) {
        Optional<EthChain> chainOptional = chainRepository.findByChainId(chainId);

        if (!chainOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        EthChain chain = chainOptional.get();
        EthChainDto dto = EthChainDto.builder()
                                     .name(chain.getName())
                                     .chainId(chain.getChainId())
                                     .averageBlockTime(chain.getAverageBlockTime())
                                     .build();

        List<EthChainNodeDto> collect = chain.getNodes()
                                             .stream()
                                             .map(n -> EthChainNodeDto.builder()
                                                                      .name(n.getName())
                                                                      .rpcProtocolType(n.getRpcProtocolType())
                                                                      .host(n.getRpcHost())
                                                                      .rpcPort(n.getRpcPort())
                                                                      .build()).collect(Collectors.toList());
        dto.setNodes(collect);

        return ResponseEntity.ok(dto);
    }
}