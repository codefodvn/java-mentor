package codefod.com.niodemo.niobasic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class NioFileWriteDemo {
    public static void main(String[] args) {
        String message = "Hello from Java NIO!\nWelcome to high-performance file I/O.";

        Path path = Path.of("niooutput.txt");

        try (FileChannel fileChannel = FileChannel.open(
                path,
                StandardOpenOption.CREATE,         // tạo file nếu chưa tồn tại
                StandardOpenOption.WRITE,          // mở để ghi
                StandardOpenOption.TRUNCATE_EXISTING // xóa nội dung cũ nếu đã có
        )) {
            // Convert chuỗi sang byte, rồi đưa vào ByteBuffer
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());

            // Ghi dữ liệu từ buffer vào file
            fileChannel.write(buffer);

            System.out.println("✅ Đã ghi xong vào file output.txt");

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi ghi file: " + e.getMessage());
        }
    }
}

