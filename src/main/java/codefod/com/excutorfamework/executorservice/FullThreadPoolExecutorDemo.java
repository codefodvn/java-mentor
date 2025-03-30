package codefod.com.excutorfamework.executorservice;

import java.util.concurrent.*;

public class FullThreadPoolExecutorDemo {

    public static void main(String[] args) throws InterruptedException {
        // ======== 1. Cấu hình đầy đủ ThreadPoolExecutor ========
        int corePoolSize = 2;
        int maximumPoolSize = 4;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;

        // BlockingQueue để chứa các task khi chờ thread rảnh
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(2);

        // ThreadFactory để tùy biến cách tạo Thread (VD: đặt tên thread)
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("CustomThread-" + count++);
                return t;
            }
        };

        // RejectedExecutionHandler xử lý khi task bị từ chối (queue đầy + maxPoolSize đã dùng hết)
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("❌ Task bị từ chối: " + r.toString());
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );

        // ======== 2. Nộp nhiều task để quan sát hành vi ========
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("✅ Đang chạy task " + taskId + " bằng " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000); // mô phỏng task tốn thời gian
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // ======== 3. Theo dõi trạng thái Executor ========
        new Thread(() -> {
            while (!executor.isTerminated()) {
                System.out.println("📊 Pool size: " + executor.getPoolSize()
                        + ", Active: " + executor.getActiveCount()
                        + ", Completed: " + executor.getCompletedTaskCount()
                        + ", Queue: " + executor.getQueue().size());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        // ======== 4. Đóng Executor sau khi task hoàn thành ========
        executor.shutdown(); // không nhận thêm task mới
        executor.awaitTermination(1, TimeUnit.HOURS); // đợi tất cả task xong
        System.out.println("🏁 Tất cả task đã hoàn thành. Executor dừng.");
    }
}
