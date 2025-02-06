package codefod.com.classloader2;

public class TestClass {

    // Biến tĩnh (static variable)
    static int staticVariable;

    // Khối khởi tạo tĩnh (static block)
    static {
        System.out.println("Static block is executed!");
        staticVariable = 42; // Khởi tạo giá trị cho biến tĩnh
    }

    // Phương thức khởi tạo (constructor)
    public TestClass() {
        System.out.println("TestClass constructor is executed!");
    }

    // Phương thức thông thường
    public void showMessage() {
        System.out.println("Hello from Codefod!");
    }

    // Phương thức tĩnh
    public static void staticMethod() {
        System.out.println("staticMethod is called!");
    }

    // Phương thức thông thường
    public void instanceMethod() {
        System.out.println("instanceMethod is called!");
    }

    // Phương thức với method reference
    public static void methodReferenceTest() {
        System.out.println("methodReferenceTest is called!");
    }
}

