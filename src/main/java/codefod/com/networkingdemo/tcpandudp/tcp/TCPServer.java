package codefod.com.networkingdemo.tcpandudp.tcp;

// TCPServer.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("🟢 TCP Server đang lắng nghe tại cổng 1234...");

        Socket clientSocket = serverSocket.accept();
        System.out.println("🔌 Client đã kết nối: " + clientSocket.getInetAddress());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String msg = in.readLine();
        System.out.println("📥 Nhận từ client: " + msg);
        out.println("📤 Server trả lời: Đã nhận [" + msg + "]");

        clientSocket.close();
        serverSocket.close();
    }
}

