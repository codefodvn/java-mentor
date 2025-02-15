package codefod.com.garbage_collectors;

public class GCRootExample {
    // GC Root 1: Biến static
    private static Object staticRoot = new Object();

    public static void main(String[] args) {
        // GC Root 2: Biến cục bộ trong stack của luồng đang chạy (main thread)
        Object localRoot = new Object();

        // GC Root 3: Active thread
        Thread thread = new Thread(() -> {
            Object threadLocalRoot = new Object(); // GC Root trong luồng con
        });
        thread.start();

        // GC Root 4: JNI Reference (giả định có native code sử dụng)
        // Giả sử có một native method tham chiếu đến đối tượng này
        Object jniReference = new Object();
    }
}