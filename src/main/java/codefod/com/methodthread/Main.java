package codefod.com.methodthread;

public class Main {
    static class MyThread extends Thread {
        private String threadName;

        public MyThread(String name) {
            this.threadName = name;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 5; i++) {
                    System.out.println(threadName + " đang chạy bước " + i);
                    Thread.sleep(500); // Tạm dừng thread hiện tại 500ms
                }
            } catch (InterruptedException e) {
                System.out.println(threadName + " bị gián đoạn!");
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MyThread t1 = new MyThread("🧵 Thread 1");
        MyThread t2 = new MyThread("🧵 Thread 2");

        // Ưu tiên cao hơn cho t2
        t2.setPriority(Thread.MAX_PRIORITY); // Giá trị = 10
        t1.setPriority(Thread.MIN_PRIORITY); // Giá trị = 1

        t1.start(); // Tạo thread và gọi run()
        t2.start();

        // Chờ thread t1 hoàn thành rồi mới tiếp tục main thread
        t1.join();

        System.out.println("👉 Main thread đã chờ xong Thread 1");

        // Gián đoạn thread nếu đang ngủ (sleep)
        t2.interrupt();
    }
}
