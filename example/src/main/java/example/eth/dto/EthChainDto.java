package example.eth.dto;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Data
public class EthChainDto {

    @NotBlank
    private String name;

    @NotBlank
    private String chainId;

    @Min(1000L)
    private Long averageBlockTime;

    @Builder
    public EthChainDto(@NotBlank String name, @NotBlank String chainId,
                       @Min(1000L) Long averageBlockTime) {
        this.name = name;
        this.chainId = chainId;
        this.averageBlockTime = averageBlockTime;
    }

    private List<EthChainNodeDto> nodes;
}
