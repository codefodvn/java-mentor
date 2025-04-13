package codefod.com.networkingdemo.sslandtsl;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

// Client kết nối HTTPS bằng SSLSocket
public class SimpleTLSClient {
    public static void main(String[] args) {
        String host = "www.example.com";
        int port = 443;

        try {
            // 1. Lấy factory tạo socket TLS
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

            // 2. Bắt tay TLS
            socket.startHandshake();
            System.out.println("✅ TLS Handshake thành công với " + host);

            // 3. Gửi HTTP request
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            writer.write("GET / HTTP/1.1\r\n");
            writer.write("Host: " + host + "\r\n");
            writer.write("Connection: close\r\n");
            writer.write("\r\n");
            writer.flush();

            // 4. Nhận phản hồi từ server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 5. Đóng socket
            writer.close();
            reader.close();
            socket.close();

        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

