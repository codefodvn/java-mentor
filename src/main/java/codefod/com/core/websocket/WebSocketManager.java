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

/**
 * Lớp quản lý WebSocket - chịu trách nhiệm xử lý kết nối, nâng cấp giao thức,
 * và điều phối các tin nhắn WebSocket.
 */
public class WebSocketManager {
    // Lưu trữ các endpoint được đăng ký theo đường dẫn
    private final Map<String, WebSocketEndpoint> endpoints = new HashMap<>();

    // Lưu trữ các phiên WebSocket đang hoạt động, sử dụng ConcurrentHashMap để an toàn đa luồng
    private final Map<SocketChannel, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // GUID chuẩn cho WebSocket, sử dụng trong quá trình bắt tay và xác thực kết nối
    private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /**
     * Đăng ký một endpoint WebSocket mới với đường dẫn xác định
     *
     * @param path     Đường dẫn URL để liên kết với endpoint
     * @param endpoint Đối tượng xử lý các sự kiện WebSocket
     */
    public void registerEndpoint(String path, WebSocketEndpoint endpoint) {
        endpoints.put(path, endpoint);
    }

    /**
     * Xử lý quá trình nâng cấp kết nối từ HTTP sang WebSocket
     *
     * @param request  Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @param channel  Kênh socket đang xử lý
     * @param key      SelectionKey liên kết với kênh
     * @return true nếu nâng cấp thành công, false nếu thất bại
     * @throws IOException khi có lỗi I/O
     */
    public boolean handleUpgrade(HttpRequest request, HttpResponse response, SocketChannel channel, SelectionKey key) throws IOException {
        // Kiểm tra xem đây có phải là yêu cầu nâng cấp WebSocket hợp lệ không
        if (!"GET".equals(request.getMethod()) ||
                !"websocket".equalsIgnoreCase(request.getHeader("Upgrade")) ||
                !request.getHeader("Connection").toLowerCase().contains("upgrade")) {
            return false;
        }

        // Tìm endpoint tương ứng với đường dẫn được yêu cầu
        WebSocketEndpoint endpoint = endpoints.get(request.getPath());
        if (endpoint == null) {
            return false;
        }

        // Kiểm tra và xử lý khóa WebSocket từ tiêu đề yêu cầu
        String wsKey = request.getHeader("Sec-WebSocket-Key");
        if (wsKey == null) {
            return false;
        }

        try {
            // Tạo khóa chấp nhận theo đặc tả WebSocket
            String acceptKey = generateAcceptKey(wsKey);

            // Xây dựng phản hồi bắt tay WebSocket
            response.setStatusCode(101); // 101 = Switching Protocols
            response.setHeader("Upgrade", "websocket");
            response.setHeader("Connection", "Upgrade");
            response.setHeader("Sec-WebSocket-Accept", acceptKey);

            // Gửi phản hồi bắt tay đến client
            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
            channel.write(responseBuffer);

            // Đăng ký phiên WebSocket mới
            WebSocketSession session = new WebSocketSession(channel, endpoint);
            sessions.put(channel, session);

            // Thay đổi chế độ quan tâm của kênh sang chế độ đọc
            key.interestOps(SelectionKey.OP_READ);

            // Gọi sự kiện onOpen trên endpoint
            endpoint.onOpen(session);

            return true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo khóa chấp nhận cho quá trình bắt tay WebSocket
     * Kết hợp khóa của client với GUID chuẩn, sau đó mã hóa bằng SHA-1 và Base64
     *
     * @param key Khóa từ client
     * @return Khóa chấp nhận được mã hóa
     * @throws NoSuchAlgorithmException khi thuật toán SHA-1 không khả dụng
     */
    private String generateAcceptKey(String key) throws NoSuchAlgorithmException {
        String concatKey = key + WEBSOCKET_GUID;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return Base64.getEncoder().encodeToString(sha1.digest(concatKey.getBytes()));
    }

    /**
     * Xử lý khung dữ liệu WebSocket nhận được từ client
     *
     * @param channel Kênh socket gửi dữ liệu
     * @param buffer  Buffer chứa dữ liệu nhận được
     * @throws IOException khi có lỗi I/O
     */
    public void processWebSocketFrame(SocketChannel channel, ByteBuffer buffer) throws IOException {
        // Lấy phiên từ map theo kênh socket
        WebSocketSession session = sessions.get(channel);
        if (session == null) {
            return;
        }

        // Phân tích khung WebSocket từ buffer
        WebSocketFrame frame = WebSocketFrame.parse(buffer);

        // Xử lý các loại khung khác nhau
        if (frame.isClose()) {
            // Xử lý khung đóng kết nối
            session.getEndpoint().onClose(session, frame.getCloseCode(), frame.getCloseReason());
            sessions.remove(channel);
            channel.close();
        } else if (frame.isPing()) {
            // Phản hồi khung ping bằng khung pong
            session.sendPong(frame.getPayloadData());
        } else if (frame.isText()) {
            // Xử lý tin nhắn văn bản
            session.getEndpoint().onMessage(session, frame.getTextPayload());
        } else if (frame.isBinary()) {
            // Xử lý tin nhắn nhị phân
            session.getEndpoint().onBinary(session, frame.getPayloadData());
        }
    }

    /**
     * Xóa phiên WebSocket khi kênh đóng hoặc bị lỗi
     *
     * @param channel Kênh socket cần loại bỏ
     */
    public void removeSession(SocketChannel channel) {
        WebSocketSession session = sessions.remove(channel);
        if (session != null) {
            try {
                // Thông báo endpoint về việc đóng kết nối bất thường
                session.getEndpoint().onClose(session, 1006, "Connection closed abnormally");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Kiểm tra xem một kênh socket có phải là phiên WebSocket không
     *
     * @param channel Kênh socket cần kiểm tra
     * @return true nếu kênh là phiên WebSocket, false nếu không phải
     */
    public boolean isWebSocketSession(SocketChannel channel) {
        return sessions.containsKey(channel);
    }
}