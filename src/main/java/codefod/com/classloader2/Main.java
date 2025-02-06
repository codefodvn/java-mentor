package codefod.com.classloader2;

import java.lang.reflect.Method;

public class Main {

    public static void main(String[] args) {
        try {
            // Bước 1: Tạo một đối tượng của TestClass
            // Quá trình class loading sẽ diễn ra ở đây.
            System.out.println("Before loading TestClass.");
            Class<?> clazz = Class.forName("codefod.com.classloader2.TestClass"); // Loading class
            System.out.println("After loading TestClass.");

            // Bước 2: Tạo một đối tượng TestClass
            Object obj = clazz.getDeclaredConstructor().newInstance(); // Initializing the class

            // Bước 3: Gọi phương thức của lớp
            clazz.getMethod("showMessage").invoke(obj);

            // Bước 4: Gọi phương thức thông qua method reference
            Method staticMethod = clazz.getMethod("staticMethod"); // Method reference
            staticMethod.invoke(null); // Gọi phương thức tĩnh

            // Bước 5: Gọi phương thức instanceMethod thông qua method reference
            Method instanceMethod = clazz.getMethod("instanceMethod"); // Method reference
            instanceMethod.invoke(obj); // Gọi phương thức thông qua đối tượng

            // Bước 6: Gọi phương thức methodReferenceTest thông qua method reference
            Method methodReferenceTest = clazz.getMethod("methodReferenceTest"); // Method reference
            methodReferenceTest.invoke(null); // Gọi phương thức tĩnh
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
