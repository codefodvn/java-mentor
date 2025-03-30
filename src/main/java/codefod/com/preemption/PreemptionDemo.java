package codefod.com.preemption;

public class PreemptionDemo {
    public static void main(String[] args) {
        Runnable task = () -> {
            while (true) {
                System.out.println(Thread.currentThread().getName() + " running");
                // Không sleep -> giữ CPU
            }
        };

        Thread t1 = new Thread(task, "Thread-A");
        Thread t2 = new Thread(task, "Thread-B");

        t1.start();
        t2.start();
    }
}