package codefod.com.garbage_collectors;


import java.util.ArrayList;
import java.util.List;

class MyObject {

    private final int id;

    public MyObject(int id) {
        this.id = id;
        System.out.println("Đối tượng " + id + " được tạo.");
    }
}

public class GCDemo {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(10000); // Dừng 5 giây để quan sát

        List<MyObject> objects = new ArrayList<>();

        // Tạo 5 đối tượng và thêm vào danh sách
        for (int i = 1; i <= 1000; i++) {
            objects.add(new MyObject(i));
        }

        System.out.println("\n--- Trước khi xóa tham chiếu ---");
        printMemoryStats();
        Thread.sleep(5000); // Dừng 3 giây để quan sát

        // Xóa tham chiếu đến các đối tượng
        objects.clear();
        System.out.println("\n--- Sau khi xóa tham chiếu ---");
        printMemoryStats();

        Thread.sleep(5000); // Dừng 5 giây để quan sát

        // Gợi ý JVM chạy GC (không đảm bảo chạy ngay)
        System.gc();

        // Đợi để GC có thời gian xử lý
        Thread.sleep(5000); // Dừng 5 giây để quan sát

        System.out.println("\n--- Kết thúc chương trình ---");
        printMemoryStats();


        try {
            Thread.sleep(1000000); // Dừng 5 giây để quan sát
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức in thông tin bộ nhớ
    private static void printMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory() / 1024;
        long totalMemory = runtime.totalMemory() / 1024;
        long usedMemory = totalMemory - freeMemory;

        System.out.println("Bộ nhớ đã dùng: " + usedMemory + " KB");
        System.out.println("Bộ nhớ trống: " + freeMemory + " KB");
    }
}
