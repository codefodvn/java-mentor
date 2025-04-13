package codefod.com.iodemo.serializationdemo;

import java.io.*;

public class SerializeExample {
    public static void main(String[] args) {
        // Tạo đối tượng Person
        Person person = new Person("John Doe", 30);

        // Đối tượng sẽ được ghi vào file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("person.ser"))) {
            // Ghi đối tượng vào file
            oos.writeObject(person);
            System.out.println("Đối tượng đã được ghi vào file person.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
