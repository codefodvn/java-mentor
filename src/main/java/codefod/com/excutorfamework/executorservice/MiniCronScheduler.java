package codefod.com.excutorfamework.executorservice;

import java.time.*;
import java.util.concurrent.*;
import java.util.*;

/*
┌──────── min (0–59)
│ ┌────── hour (0–23)
│ │ ┌──── day of month (1–31)
│ │ │ ┌── month (1–12)
│ │ │ │ ┌─ day of week (0–6) (0 = Chủ Nhật)
│ │ │ │ │
│ │ │ │ │
* * * * *
 */
public class MiniCronScheduler {

    public static void main(String[] args) {
        String cron = "30 9 * * *"; // Mỗi ngày lúc 9:30

        scheduleCron(cron, () -> {
            System.out.println("🔔 [CRON] Đến giờ chạy task lúc " + new Date());
        });
    }

    public static void scheduleCron(String expression, Runnable task) {
        String[] fields = expression.split(" ");
        if (fields.length != 5) {
            throw new IllegalArgumentException("❌ Cron expression phải có 5 trường: min hour day month weekday");
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();

            int minute = now.getMinute();
            int hour = now.getHour();
            int day = now.getDayOfMonth();
            int month = now.getMonthValue();
            int weekday = now.getDayOfWeek().getValue() % 7; // Java: 1=Monday → 0=Sunday

            if (match(fields[0], minute) &&
                    match(fields[1], hour) &&
                    match(fields[2], day) &&
                    match(fields[3], month) &&
                    match(fields[4], weekday)) {

                System.out.println("✅ [MATCH] Cron khớp lúc " + now);
                task.run();
            } else {
                System.out.println("🕒 Cron chưa khớp: " + now);
            }
        }, 0, 30, TimeUnit.SECONDS); // kiểm tra mỗi 30s
    }

    private static boolean match(String field, int value) {
        if (field.equals("*")) return true;
        try {
            return Integer.parseInt(field) == value;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}