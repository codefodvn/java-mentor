package codefod.com.networkingdemo.nio.advance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class NIOChatServer {
    private static final int PORT = 12345;
    private static final Map<SocketChannel, String> userMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("🚀 NIO Chat Server chạy tại cổng " + PORT);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()) {
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    client.write(ByteBuffer.wrap("🎉 Nhập tên của bạn:\n".getBytes()));
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    buffer.clear();
                    int bytesRead = client.read(buffer);

                    if (bytesRead == -1) {
                        disconnect(client);
                        continue;
                    }

                    buffer.flip();
                    String msg = new String(buffer.array(), 0, bytesRead).trim();

                    if (!userMap.containsKey(client)) {
                        userMap.put(client, msg);
                        String joinMsg = "🔔 " + msg + " đã tham gia phòng chat\n";
                        System.out.println("👤 " + msg + " đã tham gia");
                        broadcast(joinMsg, client);
                    } else {
                        String sender = userMap.get(client);
                        String formatted = "💬 [" + sender + "]: " + msg + "\n";
                        System.out.print(formatted);
                        broadcast(formatted, client);
                    }
                }
            }
        }
    }

    private static void broadcast(String message, SocketChannel except) throws IOException {
        ByteBuffer msgBuffer = ByteBuffer.wrap(message.getBytes());
        for (SocketChannel client : userMap.keySet()) {
            if (client != except && client.isOpen()) {
                msgBuffer.rewind();
                client.write(msgBuffer);
            }
        }
    }

    private static void disconnect(SocketChannel client) throws IOException {
        String name = userMap.getOrDefault(client, "Người dùng");
        System.out.println("❌ " + name + " đã rời phòng chat.");
        userMap.remove(client);
        client.close();
        String left = "❌ " + name + " đã rời phòng chat\n";
        broadcast(left, null);
    }
}

