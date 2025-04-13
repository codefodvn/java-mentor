package codefod.com.iodemo.serializationdemo;

import java.io.*;

// Đối tượng sẽ được ghi vào file, do đó cần phải implements Serializable
class Person implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // Thêm serialVersionUID để đảm bảo tính tương thích khi deserialization
    private String name;
    private int age;

    // Constructor
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Getter và Setter
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + '}';
    }
}

