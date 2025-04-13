package codefod.com.networkingdemo.tcpandudp.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {
    public static void main(String[] args) {
        final int PORT = 1234;
        byte[] buffer = new byte[1024];

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("🟢 UDP Server lắng nghe tại cổng " + PORT);

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String message = new String(request.getData(), 0, request.getLength());
                System.out.println("📥 Nhận: " + message);

                // Tạo phản hồi
                String response = "✅ Server đã nhận: " + message;
                byte[] responseBytes = response.getBytes();

                DatagramPacket reply = new DatagramPacket(
                        responseBytes, responseBytes.length,
                        request.getAddress(), request.getPort());

                socket.send(reply);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

