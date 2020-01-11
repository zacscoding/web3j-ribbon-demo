package example.eth.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import example.eth.domain.RpcProtocolType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EthChainNodeDto {

    @NotBlank
    private String name;

    @NotBlank
    private RpcProtocolType rpcProtocolType;

    @NotBlank
    private String host;

    @Min(1)
    private int rpcPort;
}
