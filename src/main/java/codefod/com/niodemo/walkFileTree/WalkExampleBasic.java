package codefod.com.niodemo.walkFileTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class WalkExampleBasic {
    public static void main(String[] args) {
        Path root = Paths.get("");

        try (Stream<Path> stream = Files.walk(root)) {
            stream.forEach(path -> System.out.println("📄 " + path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

