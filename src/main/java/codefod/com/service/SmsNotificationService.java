package codefod.com.service;

public class SmsNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        System.out.println("Sms notification: " + message);
    }
}
