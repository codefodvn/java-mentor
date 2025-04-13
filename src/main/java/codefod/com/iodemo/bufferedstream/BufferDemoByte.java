package codefod.com.iodemo.bufferedstream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BufferDemoByte {
    public static void main(String[] args) {
        String inputFile = "input.txt";
        String outputFile = "output.txt";

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            int byteData;
            while ((byteData = bis.read()) != -1) {
                bos.write(byteData);  // Ghi byte vào output file
            }
            System.out.println("Dữ liệu đã được sao chép thành công từ " + inputFile + " sang " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

