package codefod.com.iodemo.serializationdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class DeserializeExample {
    public static void main(String[] args) {
        // Đọc đối tượng từ file
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("person.ser"))) {
            // Đọc đối tượng từ file
            Person person = (Person) ois.readObject();
            System.out.println("Đối tượng được deserialized: " + person);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
