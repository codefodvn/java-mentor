package codefod.com.volatile_keyword.use;

public class Main {
    static volatile boolean stop = false; // thêm volatile

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            System.out.println("Worker bắt đầu...");
            while (!stop) {
                // busy wait
            }
            System.out.println("Worker dừng!");
        });

        worker.start();

        Thread.sleep(1000);
        stop = true; // giờ mọi thread đều thấy được thay đổi này
        System.out.println("Main thread đổi stop = true");
    }
}
