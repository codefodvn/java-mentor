package codefod.com.niodemo.niobasic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class NioFileReadDemo {
    public static void main(String[] args) {
        Path path = Path.of("example.txt");

        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024); // Bộ nhớ đệm 1KB

            while (fileChannel.read(buffer) > 0) {
                buffer.flip(); // chuyển sang chế độ đọc

                while (buffer.hasRemaining()) {
                    System.out.print((char) buffer.get()); // đọc từng byte rồi ép thành char
                }

                buffer.clear(); // chuẩn bị cho lần đọc tiếp theo
            }

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi đọc file: " + e.getMessage());
        }
    }
}

