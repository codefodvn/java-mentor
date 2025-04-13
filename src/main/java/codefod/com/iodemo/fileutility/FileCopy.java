package codefod.com.iodemo.fileutility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class FileCopy {
    public static void main(String[] args) {
        // Khởi tạo Scanner để nhận đầu vào từ người dùng
        Scanner scanner = new Scanner(System.in);

        // Nhập tên file nguồn và file đích
        System.out.print("Nhập tên file nguồn: ");
        String sourceFile = scanner.nextLine();

        System.out.print("Nhập tên file đích: ");
        String destFile = scanner.nextLine();

        // Thực hiện sao chép file
        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile))
        ) {
            int data;
            // Đọc dữ liệu từ file nguồn và ghi vào file đích
            while ((data = bis.read()) != -1) {
                bos.write(data);
            }
            System.out.println("Copy file thành công.");
        } catch (FileNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy file nguồn.");
        } catch (IOException e) {
            System.err.println("Lỗi: " + e.getMessage());
        }
    }
}

