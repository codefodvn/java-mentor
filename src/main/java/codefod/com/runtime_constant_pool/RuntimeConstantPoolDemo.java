package codefod.com.runtime_constant_pool;

public class RuntimeConstantPoolDemo {

    final static int A = 10;   // ✅ Lưu trong Runtime Constant Pool
    final static double B = 3.14;  // ✅ Lưu trong Runtime Constant Pool
    final static String S1 = "Hello";  // ✅ Lưu trong Runtime Constant Pool

    int x = 100;  // ❌ Không lưu trong Runtime Constant Pool (Lưu trong Heap)
    String S2 = new String("Hello");  // ❌ Không lưu trong Runtime Constant Pool (Lưu trong Heap)

    public static void main(String[] args) {
        System.out.println(A);
        System.out.println(S1);
    }
}
