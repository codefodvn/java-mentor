package codefod.com.Interthreadcommunication;

// Ở đây nó hoạt động trên cùng 1 object đó là this. Chính là SharedBuffer => đây chính là object monitor
// Vì chúng ta đang gọi wait() và notify() bên trong synchronized method,
// mà synchronized không chỉ rõ object nào → nên mặc định là synchronized(this).
public class Main {
    static class SharedBuffer {
        private int data;
        private boolean hasData = false;

        // Consumer gọi: lấy dữ liệu
        public synchronized int get() {
            while (!hasData) {
                try {
                    wait(); // Chờ đến khi có dữ liệu
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            hasData = false;
            notify(); // Đánh thức producer
            return data;
        }

        // Producer gọi: đưa dữ liệu
        public synchronized void put(int value) {
            while (hasData) {
                try {
                    wait(); // Chờ đến khi buffer trống
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            data = value;
            hasData = true;
            notify(); // Đánh thức consumer
        }
    }

    public static void main(String[] args) {
        SharedBuffer buffer = new SharedBuffer();

        // Producer
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                buffer.put(i);
                System.out.println("Producer đặt: " + i);
                try { Thread.sleep(300); } catch (InterruptedException _) {}
            }
        });

        // Consumer
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                int val = buffer.get();
                System.out.println("Consumer nhận: " + val);
                try { Thread.sleep(500); } catch (InterruptedException _) {}
            }
        });

        producer.start();
        consumer.start();
    }
}
