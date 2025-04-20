package codefod.com.core.websocket;

import java.io.IOException;

public interface WebSocketEndpoint {
    void onOpen(WebSocketSession session) throws IOException;
    void onMessage(WebSocketSession session, String message) throws IOException;
    void onBinary(WebSocketSession session, byte[] data) throws IOException;
    void onClose(WebSocketSession session, int code, String reason) throws IOException;
    void onError(WebSocketSession session, Throwable throwable);
}
