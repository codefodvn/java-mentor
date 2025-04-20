package codefod.com.socket;

import codefod.com.core.websocket.WebSocketEndpoint;
import codefod.com.core.websocket.WebSocketSession;

import java.io.IOException;

public class ChatWebSocketEndpoint implements WebSocketEndpoint {
    @Override
    public void onOpen(WebSocketSession session) throws IOException {
        System.out.println("WebSocket connection opened: " + session.getId());
        session.sendText("Welcome to the chat server!");
    }

    @Override
    public void onMessage(WebSocketSession session, String message) throws IOException {
        System.out.println("Received message: " + message);

        // Echo the message back
        session.sendText("You said: " + message);
    }

    @Override
    public void onBinary(WebSocketSession session, byte[] data) throws IOException {
        // Handle binary data
    }

    @Override
    public void onClose(WebSocketSession session, int code, String reason) throws IOException {
        System.out.println("WebSocket connection closed: " + session.getId() +
                ", Code: " + code + ", Reason: " + reason);
    }

    @Override
    public void onError(WebSocketSession session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
        throwable.printStackTrace();
    }
}