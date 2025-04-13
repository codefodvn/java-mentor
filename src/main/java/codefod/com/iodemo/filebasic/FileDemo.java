package codefod.com.iodemo.filebasic;

import java.io.File;
import java.io.IOException;

public class FileDemo {
    public static void main(String[] args) {
        // Đường dẫn file và thư mục
        String filePath = "test.txt";
        String dirPath = "testDir";

        // Kiểm tra file có tồn tại không
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("File đã tồn tại: " + filePath);
        } else {
            System.out.println("File không tồn tại: " + filePath);
        }

        // Tạo file mới
        try {
            if (file.createNewFile()) {
                System.out.println("File đã được tạo: " + filePath);
            } else {
                System.out.println("File đã tồn tại, không thể tạo mới.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Tạo thư mục
        File dir = new File(dirPath);
        if (dir.mkdir()) {
            System.out.println("Thư mục đã được tạo: " + dirPath);
        } else {
            System.out.println("Không thể tạo thư mục: " + dirPath);
        }

        // Tạo thư mục cùng với các thư mục con
        File nestedDir = new File("testDir/subDir");
        if (nestedDir.mkdirs()) {
            System.out.println("Các thư mục đã được tạo: testDir/subDir");
        } else {
            System.out.println("Không thể tạo thư mục con.");
        }

        // Liệt kê các file trong thư mục
        File folder = new File("testDir");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                System.out.println("Danh sách các file trong thư mục " + folder.getName() + ":");
                for (File f : files) {
                    System.out.println(f.getName());
                }
            }
        }

        // Xóa file
        if (file.delete()) {
            System.out.println("File đã bị xóa: " + filePath);
        } else {
            System.out.println("Không thể xóa file.");
        }

        // Xóa thư mục
        if (dir.delete()) {
            System.out.println("Thư mục đã bị xóa: " + dirPath);
        } else {
            System.out.println("Không thể xóa thư mục.");
        }
    }
}
