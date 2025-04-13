package codefod.com.networkingdemo.tcpandudp.tcp;

// TCPClient.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1234);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        out.println("Xin chào từ TCP Client");
        String response = in.readLine();
        System.out.println("📩 Phản hồi từ server: " + response);

        socket.close();
    }
}

