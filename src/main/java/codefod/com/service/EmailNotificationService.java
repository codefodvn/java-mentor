package codefod.com.service;

public class EmailNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        System.out.println("Email notification: " + message);
    }
}
