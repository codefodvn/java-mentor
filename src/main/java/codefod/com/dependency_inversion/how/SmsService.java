package codefod.com.dependency_inversion.how;

public class SmsService extends NotificationService {

    @Override
    public void sendNotification() {
        System.out.println("Sending an SMS notification");
    }

}
