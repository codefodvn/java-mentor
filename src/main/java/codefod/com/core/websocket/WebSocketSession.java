package codefod.com.core.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebSocketSession {
    private final String id;
    private final SocketChannel channel;
    private final WebSocketEndpoint endpoint;
    private final Map<String, Object> attributes = new HashMap<>();

    public WebSocketSession(SocketChannel channel, WebSocketEndpoint endpoint) {
        this.id = UUID.randomUUID().toString();
        this.channel = channel;
        this.endpoint = endpoint;
    }

    public String getId() {
        return id;
    }

    public WebSocketEndpoint getEndpoint() {
        return endpoint;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void sendText(String message) throws IOException {
        sendFrame(WebSocketFrame.createTextFrame(message));
    }

    public void sendBinary(byte[] data) throws IOException {
        sendFrame(WebSocketFrame.createBinaryFrame(data));
    }

    public void sendPing(byte[] data) throws IOException {
        sendFrame(WebSocketFrame.createPingFrame(data));
    }

    public void sendPong(byte[] data) throws IOException {
        sendFrame(WebSocketFrame.createPongFrame(data));
    }

    public void close() throws IOException {
        close(1000, "Normal closure");
    }

    public void close(int code, String reason) throws IOException {
        sendFrame(WebSocketFrame.createCloseFrame(code, reason));
        channel.close();
    }

    private void sendFrame(WebSocketFrame frame) throws IOException {
        ByteBuffer buffer = frame.toBuffer();
        channel.write(buffer);
    }
}

