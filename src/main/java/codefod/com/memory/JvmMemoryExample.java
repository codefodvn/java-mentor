package codefod.com.memory;

public class JvmMemoryExample {

    // Static variable stored in Method Area
    private static int staticCounter = 0;

    // Instance variable stored in Heap (part of object memory)
    private String instanceVariable;

    public JvmMemoryExample(String value) {
        this.instanceVariable = value; // Object stored in Heap
    }

    public void stackMemoryExample(int number) {
        // Local primitive variable stored in Stack
        int localVar = number;

        // Object reference stored in Stack, actual object in Heap
        String str = "Constant String"; // Stored in Runtime Constant Pool

        System.out.println("Local Variable: " + localVar);
        System.out.println("String Literal (Runtime Constant Pool): " + str);
    }

    public static void main(String[] args) {
        // Stack: main method frame is created

        // Heap: New object created
        JvmMemoryExample example = new JvmMemoryExample("Heap Stored Value");

        // Stack: Method call frame for stackMemoryExample
        example.stackMemoryExample(42);

        // Static variable (Method Area) access
        staticCounter++;

        System.out.println("Instance Variable (Heap): " + example.instanceVariable);
        System.out.println("Static Variable (Method Area): " + staticCounter);

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
