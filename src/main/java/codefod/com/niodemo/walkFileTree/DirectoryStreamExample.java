package codefod.com.niodemo.walkFileTree;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// duyệt file trong thư mục hiện tại
public class DirectoryStreamExample {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    System.out.println("📁 Dir: " + entry.getFileName());
                } else {
                    System.out.println("📄 File: " + entry.getFileName());
                }
            }
        }
    }
}

