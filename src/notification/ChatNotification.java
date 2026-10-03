package notification;

public class ChatNotification implements AppNotification {
    private String sender;

    public ChatNotification(String sender)
    {
        this.sender=sender;
    }

    @Override
    public String getNotificationMessage()
    {
        return "New message from "+sender;
    }

}
