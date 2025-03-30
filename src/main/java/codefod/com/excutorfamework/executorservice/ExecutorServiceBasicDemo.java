package codefod.com.excutorfamework.executorservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceBasicDemo {
    public static void main(String[] args) {
        ExecutorService executor = new ThreadPoolExecutor(
                2, 4,
                10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );

        executor.execute(() -> System.out.println("Hello from ThreadPoolExecutor!"));
        executor.shutdown();
    }
}
