package codefod.com.networkingdemo.sslandtsl;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

// Java HTTPS client (dễ hơn, dùng HttpsURLConnection)
public class HttpsClient {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://www.example.com");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            System.out.println("✅ Connected to " + url);
            System.out.println("🔐 Protocol: " + conn.getCipherSuite());

            // Đọc dữ liệu phản hồi
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

        } catch (Exception e) {
            System.err.println("❌ Lỗi kết nối HTTPS: " + e.getMessage());
        }
    }
}

