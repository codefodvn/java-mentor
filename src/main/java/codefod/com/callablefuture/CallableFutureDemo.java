package codefod.com.callablefuture;

import java.util.concurrent.*;

public class CallableFutureDemo {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 1. Tạo ExecutorService (ThreadPool)
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 2. Tạo Callable task trả về kết quả
        Callable<String> task = () -> {
            System.out.println("🧵 Đang chạy task trong thread: " + Thread.currentThread().getName());
            Thread.sleep(2000); // mô phỏng công việc
            return "✅ Kết quả từ Callable!";
        };

        // 3. Submit task, nhận về Future
        Future<String> future = executor.submit(task);

        System.out.println("⏳ Đang chờ kết quả từ task...");

        // 4. Lấy kết quả (blocking)
        String result = future.get(); // chặn cho đến khi có kết quả

        System.out.println("🎉 Nhận được kết quả: " + result);

        executor.shutdown();
    }
}