package codefod.com.networkingdemo.url;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class URLReadExample {
    public static void main(String[] args) {
        try {
            // 1. Tạo đối tượng URL
            URL url = new URL("https://www.example.com");

            // 2. Mở kết nối
            URLConnection connection = url.openConnection();

            // 3. Đọc dữ liệu từ InputStream
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            String line;
            System.out.println("📥 Nội dung từ URL:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




