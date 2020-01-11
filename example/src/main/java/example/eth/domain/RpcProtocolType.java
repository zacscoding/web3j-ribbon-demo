package example.eth.domain;

/**
 * Rpc protocol type enum
 */
public enum RpcProtocolType {

    HTTP("http"), HTTPS("https"),
    WEBSOCKET("ws");

    private String scheme;

    RpcProtocolType(String scheme) {
        this.scheme = scheme;
    }

    public String getScheme() {
        return scheme;
    }
}
