package codefod.com.excutorfamework.executorservice;

import java.util.concurrent.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DailyScheduler {

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable dailyTask = () -> {
            System.out.println("🔔 Chạy task hàng ngày lúc 9h sáng: " + new Date());
            // TODO: Thêm logic bạn muốn chạy mỗi ngày ở đây
        };

        long initialDelay = computeInitialDelay(9, 0); // giờ 9:00

        long period = TimeUnit.DAYS.toSeconds(1); // lặp lại mỗi 24h

        scheduler.scheduleAtFixedRate(dailyTask, initialDelay, period, TimeUnit.SECONDS);
    }

    private static long computeInitialDelay(int targetHour, int targetMinute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0);

        // Nếu đã qua 9h sáng hôm nay → chuyển sang ngày mai
        if (now.compareTo(nextRun) >= 0) {
            nextRun = nextRun.plusDays(1);
        }

        long delay = ChronoUnit.SECONDS.between(now, nextRun);
        System.out.println("⏳ Delay ban đầu: " + delay + " giây (chạy vào lúc " + nextRun + ")");
        return delay;
    }
}