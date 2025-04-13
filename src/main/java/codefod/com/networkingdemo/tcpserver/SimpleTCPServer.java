package codefod.com.networkingdemo.tcpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// cách test: telnet localhost 12345
public class SimpleTCPServer {
    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("✅ Server đang lắng nghe tại port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Bước 2
                System.out.println("🔌 Client kết nối: " + clientSocket.getInetAddress());

                // Bước 3: Giao tiếp
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String received;
                while ((received = in.readLine()) != null) {
                    System.out.println("📥 Nhận từ client: " + received);
                    out.println("📤 Server echo: " + received); // Phản hồi lại
                }

                // Bước 4: Đóng kết nối
                clientSocket.close();
                System.out.println("❌ Client ngắt kết nối.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

