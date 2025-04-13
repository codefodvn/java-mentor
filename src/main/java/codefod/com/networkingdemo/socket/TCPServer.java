package codefod.com.networkingdemo.socket;

import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        int port = 1234;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🟢 Server đang chạy tại cổng " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Đợi client
                System.out.println("🔌 Client đã kết nối: " + clientSocket.getInetAddress());

                // Đọc dữ liệu từ client
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("📥 Nhận: " + line);
                    out.println("📤 Server trả lại: " + line);
                }

                clientSocket.close();
                System.out.println("❌ Client đã ngắt kết nối.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

