package codefod.com.synchronized_keyword;

public class Main {
    static class Counter {
        private int count = 0;

        public void increment() {
            count++; // Không đồng bộ - KHÔNG thread-safe
        }

        public int getCount() {
            return count;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Kết quả mong đợi: 2000");
        System.out.println("Kết quả thực tế: " + counter.getCount()); // có thể < 2000
    }
}
