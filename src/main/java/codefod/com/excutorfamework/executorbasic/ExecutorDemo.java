package codefod.com.excutorfamework.executorbasic;

import java.util.concurrent.Executor;

public class ExecutorDemo {
    public static void main(String[] args) {
        Executor executor = command -> new Thread(command).start();

        executor.execute(() -> {
            System.out.println("Hello from Executor!");
        });
    }
}
