package codefod.com.networkingdemo.basicwebserver.https;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class InsecureHttpsClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8443;

        try {
            // 1. Tạo TrustManager cho phép tất cả (bỏ kiểm tra chứng chỉ)
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            // 2. Khởi tạo SSLContext với TrustManager trên
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // 3. Lấy SSLSocketFactory từ context
            SSLSocketFactory factory = sslContext.getSocketFactory();

            // 4. Tạo socket kết nối tới HTTPS server
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

            // 5. Bắt tay TLS (vẫn thực hiện mã hóa)
            socket.startHandshake();
            System.out.println("🔐 Đã kết nối đến server TLS tại " + host + ":" + port);

            // 6. Gửi HTTP GET request
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            out.println("GET / HTTP/1.1");
            out.println("Host: " + host);
            out.println("Connection: close");
            out.println();

            // 7. Đọc response từ server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("📥 " + line);
            }

            // 8. Đóng kết nối
            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {
            System.err.println("❌ Lỗi HTTPS Client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

