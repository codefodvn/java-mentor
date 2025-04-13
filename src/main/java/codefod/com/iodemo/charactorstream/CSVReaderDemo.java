package codefod.com.iodemo.charactorstream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVReaderDemo {
    public static void main(String[] args) {
        // Đọc file CSV sử dụng BufferedReader
        String inputFile = "data.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            // Đọc từng dòng trong file
            while ((line = br.readLine()) != null) {
                // Chia dòng theo dấu phẩy để lấy các cột
                String[] columns = line.split(",");
                // In ra tên và tuổi (ví dụ file CSV có 2 cột: Tên, Tuổi)
                System.out.println("Tên: " + columns[1] + ", Tuổi: " + columns[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}





