package notification;

public class FileNotification implements AppNotification {

    private final String fileName;

    public FileNotification(String fileName)
    {
        this.fileName= fileName;
    }

    @Override
    public String getNotificationMessage()
    {
        return "File received: "+fileName;
    }

}
