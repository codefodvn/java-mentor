package codefod.com.iodemo.bufferedstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BufferDemoChar {
    public static void main(String[] args) {
        String inputFile = "input.txt";
        String outputFile = "output.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();  // Thêm dòng mới
            }
            System.out.println("Dữ liệu đã được sao chép thành công từ " + inputFile + " sang " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

