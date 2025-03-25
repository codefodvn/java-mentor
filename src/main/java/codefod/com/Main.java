package codefod.com;

import codefod.com.service.EmailNotificationService;
import codefod.com.service.JobService;
import codefod.com.service.NotificationService;

public class Main {

    public static void main(String[] args) {
        NotificationService notificationService = new EmailNotificationService();
        JobService jobService = new JobService(notificationService);
        jobService.runJob();
    }
}