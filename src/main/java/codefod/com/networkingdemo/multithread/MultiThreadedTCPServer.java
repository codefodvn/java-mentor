package codefod.com.networkingdemo.multithread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadedTCPServer {

    public static void main(String[] args) {
        int port = 8888;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🟢 Server đang chạy tại cổng " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 Client kết nối từ: " + clientSocket.getInetAddress());

                // Mỗi client sẽ được xử lý trong một luồng riêng
                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Runnable xử lý từng client
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
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("📩 [" + socket.getInetAddress() + "] gửi: " + inputLine);
                    out.println("✅ Server nhận được: " + inputLine);
                }

                System.out.println("❌ Client ngắt kết nối: " + socket.getInetAddress());
                socket.close();

            } catch (IOException e) {
                System.err.println("⚠️ Lỗi với client: " + e.getMessage());
            }
        }
    }
}

