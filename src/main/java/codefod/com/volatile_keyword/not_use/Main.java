package codefod.com.volatile_keyword.not_use;

public class Main {
    static volatile boolean running = true;

    static boolean stop = false; // KHÔNG volatile

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            System.out.println("Worker bắt đầu...");
            while (!stop) {
                // busy wait
            }
            System.out.println("Worker dừng!");
        });

        worker.start();

        Thread.sleep(1000); // main thread đợi 1 giây
        stop = true;        // thay đổi biến stop
        System.out.println("Main thread đổi stop = true");
    }
}
