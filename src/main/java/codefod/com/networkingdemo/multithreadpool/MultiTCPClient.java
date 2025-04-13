package codefod.com.networkingdemo.multithreadpool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiTCPClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8888;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Nhập số lượng kết nối client cần tạo: ");
        int n = scanner.nextInt();

        ExecutorService threadPool = Executors.newFixedThreadPool(n);

        for (int i = 0; i < n; i++) {
            int clientId = i + 1;
            threadPool.submit(() -> runClient(host, port, clientId));
        }

        threadPool.shutdown(); // Không nhận thêm task mới
    }

    public static void runClient(String host, int port, int clientId) {
        try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("🤖 Client #" + clientId + " kết nối thành công.");

            // Mỗi client gửi 5 dòng test rồi thoát
            for (int i = 1; i <= 5; i++) {
                String message = "Client #" + clientId + " → Hello " + i;
                out.println(message);
                String response = in.readLine();
                System.out.println("📩 Client #" + clientId + " nhận: " + response);

                Thread.sleep(1000); // mô phỏng delay
            }

            socket.close();
            System.out.println("❌ Client #" + clientId + " ngắt kết nối.");

        } catch (IOException | InterruptedException e) {
            System.err.println("⚠️ Client #" + clientId + " lỗi: " + e.getMessage());
        }
    }
}


