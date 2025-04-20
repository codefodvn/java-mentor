package codefod.com.core.server;

import codefod.com.core.dispatcher.RequestDispatcher;
import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;
import codefod.com.core.websocket.WebSocketManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Máy chủ HTTP không đồng bộ sử dụng NIO (Non-blocking I/O) để xử lý nhiều kết nối đồng thời
 * Hỗ trợ cả yêu cầu HTTP thông thường và WebSocket
 */
public class NioHttpServer {
    // Các hằng số cấu hình
    private static final int BUFFER_SIZE = 8192;
    private static final Logger LOGGER = Logger.getLogger(NioHttpServer.class.getName());

    // Thuộc tính cơ bản của máy chủ
    private final int port;
    private final ExecutorService threadPool;
    private final RequestDispatcher dispatcher;
    private final WebSocketManager webSocketManager;

    // Trạng thái máy chủ
    private Selector selector;
    private volatile boolean running = true;

    /**
     * Khởi tạo máy chủ HTTP NIO với các thành phần cần thiết
     *
     * @param port             Cổng để lắng nghe kết nối
     * @param threadPoolSize   Kích thước của pool luồng để xử lý yêu cầu
     * @param dispatcher       Bộ điều phối xử lý các yêu cầu HTTP
     * @param webSocketManager Quản lý WebSocket (có thể null nếu không hỗ trợ WebSocket)
     */
    public NioHttpServer(int port, int threadPoolSize, RequestDispatcher dispatcher,
                         WebSocketManager webSocketManager) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.dispatcher = dispatcher;
        this.webSocketManager = webSocketManager;
    }

    /**
     * Khởi động máy chủ và bắt đầu vòng lặp xử lý sự kiện chính
     *
     * @throws IOException khi có lỗi mở kênh hoặc liên kết cổng
     */
    public void start() throws IOException {
        // Cấu hình kênh máy chủ
        ServerSocketChannel serverChannel = configureServerChannel();

        LOGGER.info("Máy chủ đã khởi động trên cổng " + port);

        // Vòng lặp chính của máy chủ
        runEventLoop();
    }

    /**
     * Cấu hình kênh socket máy chủ và đăng ký với selector
     *
     * @return ServerSocketChannel đã được cấu hình
     * @throws IOException khi có lỗi I/O
     */
    private ServerSocketChannel configureServerChannel() throws IOException {
        // Mở kênh socket máy chủ
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);

        // Tạo selector và đăng ký kênh máy chủ
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        return serverChannel;
    }

    /**
     * Chạy vòng lặp xử lý sự kiện chính của máy chủ
     *
     * @throws IOException khi có lỗi I/O trong quá trình xử lý sự kiện
     */
    private void runEventLoop() throws IOException {
        while (running) {
            // Chờ cho các sự kiện xảy ra
            selector.select();

            // Xử lý các khóa đã sẵn sàng
            processReadyKeys();
        }
    }

    /**
     * Xử lý các khóa selector đã sẵn sàng
     *
     * @throws IOException khi có lỗi I/O trong quá trình xử lý
     */
    private void processReadyKeys() throws IOException {
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (!key.isValid()) {
                continue;
            }

            // Xử lý sự kiện dựa trên loại
            if (key.isAcceptable()) {
                handleAccept(key);
            } else if (key.isReadable()) {
                handleRead(key);
            }
        }
    }

    /**
     * Xử lý sự kiện chấp nhận kết nối mới
     *
     * @param key SelectionKey cho kênh đang chấp nhận kết nối
     * @throws IOException khi có lỗi I/O
     */
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        LOGGER.fine("Đã chấp nhận kết nối mới từ: " + clientChannel.getRemoteAddress());
    }

    /**
     * Xử lý sự kiện đọc dữ liệu từ kênh
     * Chuyển việc xử lý thực tế cho thread pool để không chặn selector
     *
     * @param key SelectionKey cho kênh có dữ liệu sẵn sàng để đọc
     */
    private void handleRead(final SelectionKey key) {
        threadPool.execute(() -> {
            SocketChannel clientChannel = (SocketChannel) key.channel();

            try {
                if (isWebSocketSession(clientChannel)) {
                    processWebSocketData(clientChannel, key);
                } else {
                    processHttpRequest(clientChannel, key);
                }
            } catch (ClosedChannelException e) {
                LOGGER.log(Level.INFO, "Kênh đã đóng trước khi đọc: {0}", e.getMessage());
                closeConnection(key, clientChannel);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Lỗi I/O khi xử lý kết nối", e);
                closeConnection(key, clientChannel);
            }
        });
    }

    /**
     * Kiểm tra nếu kênh được liên kết với phiên WebSocket
     *
     * @param channel Kênh socket cần kiểm tra
     * @return true nếu là phiên WebSocket, false nếu không phải
     */
    private boolean isWebSocketSession(SocketChannel channel) {
        return webSocketManager != null && webSocketManager.isWebSocketSession(channel);
    }

    /**
     * Xử lý dữ liệu từ kết nối WebSocket
     *
     * @param channel Kênh socket chứa dữ liệu WebSocket
     * @param key SelectionKey tương ứng
     * @throws IOException khi có lỗi I/O
     */
    private void processWebSocketData(SocketChannel channel, SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead = channel.read(buffer);

        if (bytesRead > 0) {
            // Xử lý khung WebSocket
            buffer.flip();
            webSocketManager.processWebSocketFrame(channel, buffer);
        } else if (bytesRead < 0) {
            // Kết nối đã đóng
            webSocketManager.removeSession(channel);
            closeConnection(key, channel);
        }
    }

    /**
     * Xử lý yêu cầu HTTP thông thường
     *
     * @param channel Kênh socket chứa dữ liệu HTTP
     * @param key SelectionKey tương ứng
     * @throws IOException khi có lỗi I/O
     */
    private void processHttpRequest(SocketChannel channel, SelectionKey key) throws IOException {
        // Đọc toàn bộ dữ liệu yêu cầu
        String requestData = readFullRequest(channel);

        // Kiểm tra nếu kết nối đã đóng hoặc không có dữ liệu
        if (requestData == null || requestData.isEmpty()) {
            return;
        }

        // Phân tích và xử lý yêu cầu
        HttpRequest request = new HttpRequest(requestData);
        HttpResponse response = new HttpResponse();

        // Kiểm tra nếu đây là yêu cầu nâng cấp WebSocket
        if (tryWebSocketUpgrade(request, response, channel, key)) {
            return;
        }

        // Xử lý yêu cầu HTTP thông thường
        dispatcher.dispatch(request, response);

        // Gửi phản hồi và đóng kết nối
        sendResponse(channel, response);
        closeConnection(key, channel);
    }

    /**
     * Đọc toàn bộ dữ liệu từ kênh socket
     *
     * @param channel Kênh socket để đọc
     * @return Chuỗi chứa dữ liệu đọc được hoặc null nếu kết nối đã đóng
     * @throws IOException khi có lỗi I/O
     */
    private String readFullRequest(SocketChannel channel) throws IOException {
        StringBuilder requestData = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead;

        while ((bytesRead = channel.read(buffer)) > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            requestData.append(new String(bytes));
            buffer.clear();
        }

        if (bytesRead < 0) {
            // Kết nối đã đóng bởi client
            channel.close();
            return null;
        }

        return requestData.toString();
    }

    /**
     * Thử nâng cấp kết nối HTTP lên WebSocket nếu được yêu cầu
     *
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @param channel Kênh socket
     * @param key SelectionKey tương ứng
     * @return true nếu nâng cấp thành công, false nếu không phải yêu cầu nâng cấp
     * @throws IOException khi có lỗi I/O
     */
    private boolean tryWebSocketUpgrade(HttpRequest request, HttpResponse response,
                                        SocketChannel channel, SelectionKey key) throws IOException {
        return webSocketManager != null &&
                webSocketManager.handleUpgrade(request, response, channel, key);
    }

    /**
     * Gửi phản hồi HTTP qua kênh socket
     *
     * @param channel Kênh socket để gửi dữ liệu
     * @param response Phản hồi HTTP để gửi
     * @throws IOException khi có lỗi I/O
     */
    private void sendResponse(SocketChannel channel, HttpResponse response) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
        channel.write(responseBuffer);
    }

    /**
     * Đóng kết nối và hủy khóa selector
     *
     * @param key SelectionKey cần hủy
     * @param channel Kênh socket cần đóng
     */
    private void closeConnection(SelectionKey key, SocketChannel channel) {
        try {
            if (channel.isOpen()) {
                channel.close();
            }
            key.cancel();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối", e);
        }
    }

    /**
     * Dừng máy chủ và giải phóng tài nguyên
     */
    public void stop() {
        this.running = false;
        this.threadPool.shutdown();

        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Lỗi khi đóng selector", e);
        }

        LOGGER.info("Máy chủ đã dừng");
    }

    /**
     * Lấy bộ điều phối yêu cầu của máy chủ
     *
     * @return RequestDispatcher được sử dụng bởi máy chủ
     */
    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }
}