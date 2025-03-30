package codefod.com.Interthreadcommunication;

public class SameObjectDemo {
    private final Object LOCK = new Object();

    public void waitingThread() {
        new Thread(() -> {
            synchronized (LOCK) {
                try {
                    System.out.println("Thread-A: waiting on LOCK");
                    LOCK.wait(); // ✅ wait trên LOCK
                    System.out.println("Thread-A: awakened");
                } catch (InterruptedException _) {}
            }
        }).start();
    }

    public void notifyingThread() {
        new Thread(() -> {
            synchronized (LOCK) {
                System.out.println("Thread-B: notifying on LOCK");
                LOCK.notify(); // ✅ notify trên cùng object LOCK
            }
        }).start();
    }

    public static void main(String[] args) throws InterruptedException {
        SameObjectDemo demo = new SameObjectDemo();
        demo.waitingThread();
        Thread.sleep(1000); // đợi Thread-A vào wait-set
        demo.notifyingThread();
    }
}
