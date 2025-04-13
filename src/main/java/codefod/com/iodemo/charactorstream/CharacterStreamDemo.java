package codefod.com.iodemo.charactorstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterStreamDemo {
    public static void main(String[] args) {
        String inputFile = "input.txt";  // Tệp cần đọc
        String outputFile = "output.txt"; // Tệp cần ghi

        // Sử dụng BufferedReader và BufferedWriter
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            // Đọc từng dòng từ input.txt và ghi vào output.txt
            while ((line = reader.readLine()) != null) {
                writer.write(line);      // Ghi dòng vào file output
                writer.newLine();        // Thêm dòng mới
            }

            System.out.println("Dữ liệu đã được sao chép thành công từ " + inputFile + " sang " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
