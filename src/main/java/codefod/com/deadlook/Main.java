package codefod.com.deadlook;

public class Main {
    static final Object resourceA = new Object();
    static final Object resourceB = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (resourceA) {
                System.out.println("Thread 1: đã giữ resource A");

                try { Thread.sleep(100); } catch (InterruptedException e) {}

                System.out.println("Thread 1: chờ resource B");
                synchronized (resourceB) {
                    System.out.println("Thread 1: đã giữ resource B");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (resourceB) {
                System.out.println("Thread 2: đã giữ resource B");

                try { Thread.sleep(100); } catch (InterruptedException e) {}

                System.out.println("Thread 2: chờ resource A");
                synchronized (resourceA) {
                    System.out.println("Thread 2: đã giữ resource A");
                }
            }
        });

        t1.start();
        t2.start();
    }
}
