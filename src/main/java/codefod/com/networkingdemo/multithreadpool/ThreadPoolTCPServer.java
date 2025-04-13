package codefod.com.networkingdemo.multithreadpool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolTCPServer {

    private static final int PORT = 8888;
    private static final int THREAD_POOL_SIZE = 10; // Xử lý tối đa 10 client đồng thời

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🟢 Server đang chạy với thread pool tại cổng " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 Client kết nối từ: " + clientSocket.getInetAddress());

                // Submit xử lý client vào thread pool
                threadPool.submit(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown(); // Dừng thread pool khi server ngắt
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("📩 [" + socket.getInetAddress() + "] gửi: " + line);
                    out.println("✅ Server nhận được: " + line);
                }

                System.out.println("❌ Client ngắt kết nối: " + socket.getInetAddress());
                socket.close();

            } catch (IOException e) {
                System.err.println("⚠️ Lỗi khi xử lý client: " + e.getMessage());
            }
        }
    }
}

