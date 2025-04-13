package codefod.com.networkingdemo.socket;

import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 1234;

        try (Socket socket = new Socket(host, port)) {
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("🧑‍💻 Đã kết nối tới server. Gõ nội dung để gửi:");

            String userInput;
            while ((userInput = keyboard.readLine()) != null) {
                out.println(userInput); // gửi đến server
                String response = in.readLine(); // nhận lại
                System.out.println("📩 Phản hồi từ server: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

