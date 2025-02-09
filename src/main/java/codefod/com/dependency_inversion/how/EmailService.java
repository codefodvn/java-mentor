package codefod.com.dependency_inversion.how;

public class EmailService extends NotificationService {

    @Override
    public void sendNotification() {
        System.out.println("Sending an email notification");
    }

}
