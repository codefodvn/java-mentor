package codefod.com.networkingdemo.chatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Map<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private static final Map<Socket, String> clientNames = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("🟢 Server đang chạy tại cổng " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            System.out.println("🔌 Kết nối mới từ " + clientInfo);
            new Thread(new ClientHandler(socket)).start();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                out.println("🎉 Chào mừng! Nhập tên của bạn:");
                String name = in.readLine();

                clientNames.put(socket, name);
                clientWriters.put(socket, out);

                String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                System.out.println("👤 " + name + " đã đăng nhập từ " + clientInfo);

                broadcast("🔔 " + name + " đã tham gia phòng chat");

                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("💬 [" + name + "]: " + msg);
                    broadcast("💬 " + name + ": " + msg);
                }

            } catch (IOException e) {
                System.err.println("⚠️ Lỗi với client: " + e.getMessage());
            } finally {
                String name = clientNames.getOrDefault(socket, "Người dùng");
                String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

                System.out.println("❌ " + name + " (" + clientInfo + ") đã ngắt kết nối.");

                broadcast("❌ " + name + " đã rời khỏi phòng chat");

                clientWriters.remove(socket);
                clientNames.remove(socket);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // phát tin nhắn
        private void broadcast(String message) {
            for (PrintWriter writer : clientWriters.values()) {
                writer.println(message);
            }
        }
    }
}

