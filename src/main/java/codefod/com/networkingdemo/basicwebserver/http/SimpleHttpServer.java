package codefod.com.networkingdemo.basicwebserver.http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer {
    public static void main(String[] args) {
        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🚀 HTTP Server đang chạy tại http://localhost:" + port);

            while (true) {
                // 1. Chấp nhận kết nối từ client (browser)
                Socket clientSocket = serverSocket.accept();
                System.out.println("✅ Client kết nối: " + clientSocket.getInetAddress());

                // 2. Đọc HTTP request
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    System.out.println("📥 " + line); // Log request header
                }

                // 3. Gửi HTTP response
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // HTTP response body (nội dung HTML)
                String body = "<html><body><h1>Hello from Java HTTP Server</h1><p>Thành công rồi!</p></body></html>";

                // HTTP response header + body
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html; charset=UTF-8");
                out.println("Content-Length: " + body.getBytes().length);
                out.println(); // dòng trống phân cách header và body
                out.println(body);

                // 4. Đóng kết nối
                out.close();
                in.close();
                clientSocket.close();
            }

        } catch (IOException e) {
            System.err.println("❌ Lỗi Server: " + e.getMessage());
        }
    }
}

