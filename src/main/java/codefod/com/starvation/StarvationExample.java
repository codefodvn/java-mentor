package codefod.com.starvation;

public class StarvationExample {
    public static void main(String[] args) {
        final Object lock = new Object();

        // Thread cao cấp - chạy liên tục
        Runnable highPriorityTask = () -> {
            while (true) {
                synchronized (lock) {
                    System.out.println(Thread.currentThread().getName() + " is doing work.");
                    try {
                        Thread.sleep(10); // chiếm lock thường xuyên
                    } catch (InterruptedException e) {
                    }
                }
            }
        };

        // Thread thấp cấp - không chen được vào
        Runnable lowPriorityTask = () -> {
            while (true) {
                synchronized (lock) {
                    System.out.println("LOW PRIORITY THREAD finally got lock!");
                    break;
                }
            }
        };

        // Tạo nhiều thread ưu tiên cao
        for (int i = 0; i < 3; i++) {
            new Thread(highPriorityTask, "HighPriority-" + i).start();
        }

        // Thread bị đói
        new Thread(lowPriorityTask, "LowPriority").start();
    }
}
