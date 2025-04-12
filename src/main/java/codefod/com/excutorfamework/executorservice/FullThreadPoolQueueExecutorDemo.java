package codefod.com.excutorfamework.executorservice;

import java.util.concurrent.*;

public class FullThreadPoolQueueExecutorDemo {

    public static void main(String[] args) throws InterruptedException {
        // ======== 1. Cấu hình đầy đủ ThreadPoolExecutor ========
        int corePoolSize = 2;
        int maximumPoolSize = 4;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;

        // Queue chứa tối đa 96 task đang chờ
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);

        // ThreadFactory tùy chỉnh
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Worker-" + count++);
                return t;
            }
        };

        // Rejection policy nếu quá tải (2 core + 96 queue + 2 max = 100 task giới hạn)
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );

        // ======== 2. Gửi 100 task để quan sát xử lý queue ========
        for (int i = 1; i <= 200; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    System.out.println("▶ Task " + taskId + " đang chạy trên " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(300); // mô phỏng task nặng
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (RejectedExecutionException e) {
                System.out.println("❌ Task " + taskId + " bị từ chối!");
            }
        }

        // ======== 3. Theo dõi trạng thái ThreadPoolExecutor định kỳ ========
        new Thread(() -> {
            while (!executor.isTerminated()) {
                System.out.println("📊 Pool size: " + executor.getPoolSize()
                        + ", Active: " + executor.getActiveCount()
                        + ", Queue: " + executor.getQueue().size()
                        + ", Completed: " + executor.getCompletedTaskCount());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        // ======== 4. Kết thúc Executor sau khi hoàn thành ========
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        System.out.println("🏁 Tất cả task đã hoàn thành.");
    }
}
