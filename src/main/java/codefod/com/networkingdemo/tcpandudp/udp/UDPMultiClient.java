package codefod.com.networkingdemo.tcpandudp.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPMultiClient {
    public static void main(String[] args) {
        final String SERVER_HOST = "localhost";
        final int SERVER_PORT = 1234;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Nhập số lượng client muốn tạo: ");
        int clientCount = scanner.nextInt();

        ExecutorService pool = Executors.newFixedThreadPool(clientCount);

        for (int i = 1; i <= clientCount; i++) {
            final int clientId = i;
            pool.submit(() -> runClient(SERVER_HOST, SERVER_PORT, clientId));
        }

        pool.shutdown();
    }

    private static void runClient(String host, int port, int clientId) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(host);

            for (int i = 1; i <= 5; i++) {
                String message = "🤖 Client #" + clientId + " → Gửi lần " + i;
                byte[] sendData = message.getBytes();
                DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, port);
                socket.send(packet);

                // Nhận phản hồi
                byte[] buffer = new byte[1024];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);

                String reply = new String(response.getData(), 0, response.getLength());
                System.out.println("📩 Client #" + clientId + " nhận phản hồi: " + reply);

                Thread.sleep(500); // mô phỏng delay giữa các lần gửi
            }

        } catch (Exception e) {
            System.err.println("⚠️ Client #" + clientId + " gặp lỗi: " + e.getMessage());
        }
    }
}

