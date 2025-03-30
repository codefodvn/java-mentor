package codefod.com.excutorfamework.forkjoinpool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinSumDemo {

    // ✅ Task để chia và xử lý song song
    static class SumTask extends RecursiveTask<Long> {
        // Chia nhỏ liên tục cho đến khi task đủ nhỏ (tức là “đạt ngưỡng” – threshold), thì mới ngừng chia và xử lý trực tiếp.
        // ngưỡng chia nhỏ => chia nhỏ đến khi nào bằng 1000 phần tử thì sẽ không chia tiếp nữa.
        // (ví dụ 100tr phần tử chia đổi đến khi mỗi task chỉ còn 1000 phần tử để xử lý)
        // số lượng task = 100tr/100
        private int[] arr;
        private static final int THRESHOLD = 1000;  // ngưỡng chia nhỏ 1000
        private int start;
        private int end;
        private static volatile int taskCount = 0; // đếm số task đã chia

        public SumTask(int[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            int taskId = ++taskCount;

            // In thông tin mỗi task
            System.out.println("🧩 Task #" + taskId + " [" + start + " - " + end +
                    "] length=" + length +
                    " chạy bởi thread: " + Thread.currentThread().getName());

            if (length <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += arr[i];
                }
                return sum;
            }

            // Chia đôi task
            int mid = start + length / 2;
            SumTask left = new SumTask(arr, start, mid);
            SumTask right = new SumTask(arr, mid, end);

            // ⚡ fork left, compute right, rồi join
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();

            return leftResult + rightResult;
        }
    }

    public static void main(String[] args) {
        // check xem có thể dùng tối đa bao nhiêu core cpu để xử lý
        // mặc định, ForkJoinPool.commonPool() có số thread song song = number of CPUs - 1
        // Mục đích chừa 1 core cho main thread => hàm main đang chạy
        // Có thể overide lại xài hết core cpu nhé :))
        System.out.println("Core CPU" +ForkJoinPool.commonPool().getParallelism());;

        // 🧮 Tạo mảng lớn 10 triệu phần tử
        int[] arr = new int[100_000_000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = 1; // mỗi phần tử = 1 → tổng phải là 10 triệu
        }

        ForkJoinPool pool = new ForkJoinPool(); // dùng pool mặc định

        long startTime = System.currentTimeMillis();
        long total = pool.invoke(new SumTask(arr, 0, arr.length));
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("✅ Tổng: " + total);
        System.out.println("⏱️ Thời gian: " + duration + " ms");
    }
}