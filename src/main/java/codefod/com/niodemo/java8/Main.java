package codefod.com.niodemo.java8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Files.list(Paths.get("."))
                .filter(p -> p.toString().endsWith(".txt"))
                .forEach(System.out::println);
    }
}