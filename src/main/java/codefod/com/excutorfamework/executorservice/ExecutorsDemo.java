package codefod.com.excutorfamework.executorservice;

import java.util.concurrent.*;
import java.util.*;

public class ExecutorsDemo {

    public static void main(String[] args) throws InterruptedException {
        // ===== 1. SINGLE THREAD EXECUTOR =====
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

        System.out.println("▶ SingleThreadExecutor:");
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            singleExecutor.submit(() -> {
                System.out.println("  🧵 Task " + taskId + " chạy trên " + Thread.currentThread().getName());
                sleep(500);
            });
        }
        singleExecutor.shutdown();
        singleExecutor.awaitTermination(5, TimeUnit.SECONDS);

        // ===== 2. FIXED THREAD POOL =====
        ExecutorService fixedExecutor = Executors.newFixedThreadPool(3);

        System.out.println("\n▶ FixedThreadPool (3 threads):");
        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            fixedExecutor.submit(() -> {
                System.out.println("  🧵 Task " + taskId + " chạy trên " + Thread.currentThread().getName());
                sleep(500);
            });
        }
        fixedExecutor.shutdown();
        fixedExecutor.awaitTermination(5, TimeUnit.SECONDS);

        // ===== 3. CACHED THREAD POOL =====
        ExecutorService cachedExecutor = Executors.newCachedThreadPool();

        System.out.println("\n▶ CachedThreadPool (dynamic threads):");
        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            cachedExecutor.submit(() -> {
                System.out.println("  🧵 Task " + taskId + " chạy trên " + Thread.currentThread().getName());
                sleep(500);
            });
        }
        cachedExecutor.shutdown();
        cachedExecutor.awaitTermination(5, TimeUnit.SECONDS);

        // ===== 4. SCHEDULED EXECUTOR =====
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        System.out.println("\n▶ ScheduledExecutor (runs after delay + fixed rate):");

        // schedule 1 lần sau 1s
        scheduler.schedule(() -> {
            System.out.println("  ⏱ Task chạy sau 1s trên " + Thread.currentThread().getName());
        }, 1, TimeUnit.SECONDS);

        // schedule định kỳ mỗi 2s
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("  🔁 Task định kỳ trên " + Thread.currentThread().getName() + " at " + new Date());
        }, 2, 2, TimeUnit.SECONDS);

        // Cho demo chạy định kỳ trong 7s rồi dừng
        Thread.sleep(7000);
        scheduler.shutdown();
        scheduler.awaitTermination(3, TimeUnit.SECONDS);

        System.out.println("\n🏁 Demo kết thúc.");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
