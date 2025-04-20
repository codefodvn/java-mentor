package codefod.com.core.websocket;

import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketManager {
    private final Map<String, WebSocketEndpoint> endpoints = new HashMap<>();
    private final Map<SocketChannel, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public void registerEndpoint(String path, WebSocketEndpoint endpoint) {
        endpoints.put(path, endpoint);
    }

    public boolean handleUpgrade(HttpRequest request, HttpResponse response, SocketChannel channel, SelectionKey key) throws IOException {
        // Check if this is a WebSocket upgrade request
        if (!"GET".equals(request.getMethod()) ||
                !"websocket".equalsIgnoreCase(request.getHeader("Upgrade")) ||
                !request.getHeader("Connection").toLowerCase().contains("upgrade")) {
            return false;
        }

        // Find the matching endpoint
        WebSocketEndpoint endpoint = endpoints.get(request.getPath());
        if (endpoint == null) {
            return false;
        }

        // Process the WebSocket handshake
        String wsKey = request.getHeader("Sec-WebSocket-Key");
        if (wsKey == null) {
            return false;
        }

        try {
            // Create accept key
            String acceptKey = generateAcceptKey(wsKey);

            // Build handshake response
            response.setStatusCode(101);
            response.setHeader("Upgrade", "websocket");
            response.setHeader("Connection", "Upgrade");
            response.setHeader("Sec-WebSocket-Accept", acceptKey);

            // Send the handshake response
            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
            channel.write(responseBuffer);

            // Register the session
            WebSocketSession session = new WebSocketSession(channel, endpoint);
            sessions.put(channel, session);

            // Change interest ops to read
            key.interestOps(SelectionKey.OP_READ);

            // Call onOpen event
            endpoint.onOpen(session);

            return true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateAcceptKey(String key) throws NoSuchAlgorithmException {
        String concatKey = key + WEBSOCKET_GUID;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return Base64.getEncoder().encodeToString(sha1.digest(concatKey.getBytes()));
    }

    public void processWebSocketFrame(SocketChannel channel, ByteBuffer buffer) throws IOException {
        WebSocketSession session = sessions.get(channel);
        if (session == null) {
            return;
        }

        WebSocketFrame frame = WebSocketFrame.parse(buffer);

        if (frame.isClose()) {
            // Handle close frame
            session.getEndpoint().onClose(session, frame.getCloseCode(), frame.getCloseReason());
            sessions.remove(channel);
            channel.close();
        } else if (frame.isPing()) {
            // Respond with pong
            session.sendPong(frame.getPayloadData());
        } else if (frame.isText()) {
            // Handle text message
            session.getEndpoint().onMessage(session, frame.getTextPayload());
        } else if (frame.isBinary()) {
            // Handle binary message
            session.getEndpoint().onBinary(session, frame.getPayloadData());
        }
    }

    public void removeSession(SocketChannel channel) {
        WebSocketSession session = sessions.remove(channel);
        if (session != null) {
            try {
                session.getEndpoint().onClose(session, 1006, "Connection closed abnormally");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isWebSocketSession(SocketChannel channel) {
        return sessions.containsKey(channel);
    }
}