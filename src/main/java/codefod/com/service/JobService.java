package codefod.com.service;

public class JobService {
    private final NotificationService notificationService;

    public JobService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    public void runJob() {
        System.out.println("Job is running");
        notificationService.sendNotification("Hello, World!");
    }
}
