package codefod.com.multiprocess;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.*;

public class MultiProcessExample {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 2; i++) {
            int processIndex = i;

            // Mỗi process chạy ở một thread để đọc output song song
            new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder("ping", "google.com"); // bỏ -c để ping mãi
                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Process " + processIndex + " Output: " + line);
                    }
                } catch (IOException e) {
                    System.err.println("Process " + processIndex + " encountered an error: " + e.getMessage());
                }
            }).start();
        }

        // Giữ main thread chạy mãi để không kết thúc chương trình
        while (true) {
            Thread.sleep(1000);
        }
    }
}
