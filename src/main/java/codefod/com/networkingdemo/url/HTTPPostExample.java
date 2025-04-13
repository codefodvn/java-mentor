package codefod.com.networkingdemo.url;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPPostExample {
    public static void main(String[] args) {
        try {
            // 1. Chuẩn bị URL và dữ liệu
            URL url = new URL("https://httpbin.org/post");
            String postData = "name=Kinh&job=teacher";

            // 2. Mở kết nối và cấu hình
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true); // Cho phép ghi dữ liệu

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postData.length()));

            // 3. Ghi dữ liệu lên server
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes());
                os.flush();
            }

            // 4. Đọc phản hồi từ server
            int status = conn.getResponseCode();
            System.out.println("📡 Response Code: " + status);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            in.close();
            conn.disconnect();

            System.out.println("📥 Response Body:\n" + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
