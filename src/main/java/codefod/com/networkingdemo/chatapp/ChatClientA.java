package codefod.com.networkingdemo.chatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClientA {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(HOST, PORT);
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Nhận thông báo từ server
        System.out.print(in.readLine()); // Nhập tên
        String name = consoleInput.readLine();
        out.println(name); // Gửi tên đến server

        // Thread nhận tin nhắn từ server
        // Nhận tin nhắn từ server – cải tiến hiển thị
        new Thread(() -> {
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    System.out.print("\r"); // Xóa dòng nếu có dấu ">" trước đó

                    if (line.startsWith("🔔")) {
                        System.out.println("📢 [HỆ THỐNG] " + line.substring(2).trim());
                    } else if (line.startsWith("💬")) {
                        String content = line.substring(2).trim();
                        int sep = content.indexOf(":");
                        if (sep != -1) {
                            String sender = content.substring(0, sep).trim();
                            String msg = content.substring(sep + 1).trim();
                            System.out.println("👤 [" + sender + "]: " + msg);
                        } else {
                            System.out.println("💬 " + content);
                        }
                    } else {
                        System.out.println(line);
                    }
                    System.out.print("> ");
                }
            } catch (IOException e) {
                System.out.println("❌ Mất kết nối tới server.");
            }
        }).start();


        // Gửi tin nhắn từ bàn phím
        String msg;
        System.out.print("> ");
        while ((msg = consoleInput.readLine()) != null) {
            out.println(msg);
            System.out.print("> ");
        }

        socket.close();
    }
}
