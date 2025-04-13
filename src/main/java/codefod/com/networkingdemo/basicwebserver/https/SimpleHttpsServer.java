package codefod.com.networkingdemo.basicwebserver.https;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

// Tạo file keystore (server.jks) dùng lệnh keytool
/*
keytool -genkeypair -alias myserver -keyalg RSA -keysize 2048 \
 -keystore server.jks -validity 3650 \
 -storepass password -keypass password \
 -dname "CN=localhost, OU=IT, O=MyCompany, L=City, S=State, C=VN"
* */
public class SimpleHttpsServer {
    public static void main(String[] args) throws Exception {
        int port = 8443;

        // 1. Load keystore chứa SSL certificate
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreStream = new FileInputStream("server.jks")) {
            keyStore.load(keyStoreStream, "password".toCharArray());
        }

        // 2. Khởi tạo KeyManager từ keystore
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "password".toCharArray());

        // 3. Tạo SSLContext và khởi tạo nó
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        // 4. Tạo SSLServerSocketFactory từ SSLContext
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

        System.out.println("🔐 HTTPS Server đang chạy tại https://localhost:" + port);

        while (true) {
            try (SSLSocket socket = (SSLSocket) serverSocket.accept()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                // Đọc request (bỏ qua phần chi tiết)
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    System.out.println("📥 " + line);
                }

                // Trả về nội dung HTML
                String body = "<html><body><h1>🔒 Hello from HTTPS Server!</h1></body></html>";

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html; charset=UTF-8");
                out.println("Content-Length: " + body.getBytes().length);
                out.println();
                out.println(body);

                out.flush();
            }
        }
    }
}

