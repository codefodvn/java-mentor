package codefod.com.nonblocking;

import java.util.concurrent.*;

public class SingleThreadNonBlockingDemo {
    public static void main(String[] args) {
        ExecutorService singleThread = Executors.newSingleThreadExecutor();

        System.out.println("🚀 Bắt đầu main thread: " + Thread.currentThread().getName());

        // Non-blocking async task
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("🔁 Task chạy trên thread: " + Thread.currentThread().getName());
            try {
                Thread.sleep(3000); // Mô phỏng IO
            } catch (InterruptedException e) {}
            System.out.println("✅ Task xong lúc: " + System.currentTimeMillis());
        }, singleThread);

        // Không block main thread
        System.out.println("⏳ Main thread tiếp tục làm việc khác...");

        future.thenRun(() -> {
            System.out.println("🎉 Xử lý tiếp sau khi task hoàn thành (non-blocking callback)");
        });

        singleThread.shutdown();
    }
}