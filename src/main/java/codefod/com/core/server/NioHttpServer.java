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

public class NioHttpServer {
    private final int port;
    private Selector selector;
    private final ExecutorService threadPool;
    private final RequestDispatcher dispatcher;
    private volatile boolean running = true;
    private final WebSocketManager webSocketManager;


    public NioHttpServer(int port, int threadPoolSize, RequestDispatcher dispatcher,
                         WebSocketManager webSocketManager) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.dispatcher = dispatcher;
        this.webSocketManager = webSocketManager;
    }

    public void start() throws IOException {
        // Open server socket channel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);

        // Create selector and register the server socket
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port " + port);

        // Main server loop
        while (running) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(final SelectionKey key) {
        threadPool.execute(() -> {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            StringBuilder requestData = new StringBuilder();

            try {
                // 👇 Nếu là WebSocket thì xử lý frame
                if (webSocketManager != null && webSocketManager.isWebSocketSession(clientChannel)) {
                    ByteBuffer buffer = ByteBuffer.allocate(8192);
                    int read = clientChannel.read(buffer);
                    if (read > 0) {
                        buffer.flip();
                        webSocketManager.processWebSocketFrame(clientChannel, buffer);
                    } else if (read < 0) {
                        webSocketManager.removeSession(clientChannel);
                        clientChannel.close();
                        key.cancel();
                    }
                    return;
                }

                ByteBuffer buffer = ByteBuffer.allocate(8192);
                int read;
                while ((read = clientChannel.read(buffer)) > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes);
                    requestData.append(new String(bytes));
                    buffer.clear();
                }

                if (read < 0) {
                    // Connection closed by client
                    clientChannel.close();
                    key.cancel();
                    return;
                }

                if (!requestData.isEmpty()) {
                    // Parse and process the request
                    HttpRequest request = new HttpRequest(requestData.toString());
                    HttpResponse response = new HttpResponse();

                    if (webSocketManager != null && webSocketManager.handleUpgrade(request, response, clientChannel, key)) {
                        return;
                    }

                    // Process the request through the dispatcher
                    dispatcher.dispatch(request, response);

                    // Send the response
                    ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
                    clientChannel.write(responseBuffer);
                    clientChannel.close();
                    key.cancel();
                }
            } catch (ClosedChannelException e) {
                System.out.println("Channel closed before read: " + e.getMessage());
                key.cancel();
                try {
                    clientChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                try {
                    clientChannel.close();
                    key.cancel();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        this.running = false;
        this.threadPool.shutdown();
    }

    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }
}
