package codefod.com.multithread;

public class Main {

    static class MyThread extends Thread {
        private String threadName;

        public MyThread(String name) {
            this.threadName = name;
        }

        @Override
        public void run() {
            // Logic chạy khi thread được start
            for (int i = 1; i <= 5; i++) {
                System.out.println(threadName + " - bước " + i);
                try {
                    Thread.sleep(500); // nghỉ 500ms
                } catch (InterruptedException e) {
                    System.out.println(threadName + " bị gián đoạn.");
                }
            }
            System.out.println(threadName + " kết thúc.");
        }
    }

    public static void main(String[] args) {
        MyThread t1 = new MyThread("🧵 Thread 1");
        MyThread t2 = new MyThread("🧵 Thread 2");

        t1.start();
        t2.start();

        System.out.println("👉 Main thread kết thúc.");
    }
}
