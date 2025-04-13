package codefod.com.networkingdemo.nio.advance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOChatClientB {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        try (SocketChannel client = SocketChannel.open(new InetSocketAddress(host, port))) {
            client.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // Thread nhận tin nhắn từ server
            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        buffer.clear();
                        int bytesRead = client.read(buffer);
                        if (bytesRead > 0) {
                            buffer.flip();
                            String msg = new String(buffer.array(), 0, bytesRead);
                            System.out.print(msg);
                        }
                        Thread.sleep(100); // tránh 100% CPU
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("❌ Mất kết nối tới server.");
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Gửi tin nhắn từ bàn phím
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = console.readLine()) != null) {
                client.write(ByteBuffer.wrap((input + "\n").getBytes()));
            }

        } catch (IOException e) {
            System.err.println("⚠️ Lỗi kết nối đến server: " + e.getMessage());
        }
    }
}

