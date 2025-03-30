package codefod.com.runnableinterface;

public class Main {
    static class MyRunnable implements Runnable {
        private String taskName;

        public MyRunnable(String name) {
            this.taskName = name;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 5; i++) {
                System.out.println(taskName + " - bước " + i);
                try {
                    Thread.sleep(400); // nghỉ 400ms
                } catch (InterruptedException e) {
                    System.out.println(taskName + " bị gián đoạn.");
                }
            }
            System.out.println(taskName + " kết thúc.");
        }
    }

    public static void main(String[] args) {
        Runnable task1 = new MyRunnable("🔧 Task 1");
        Runnable task2 = new MyRunnable("🔧 Task 2");

        Thread t1 = new Thread(task1);
        Thread t2 = new Thread(task2);

        t1.start();
        t2.start();

        System.out.println("👉 Main thread kết thúc.");
    }
}
