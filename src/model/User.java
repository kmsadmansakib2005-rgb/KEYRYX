package model;

public class User {

    private String userName;
    private boolean online;

    public User(String userName, boolean online)
    {
        this.userName= userName;
        this.online= online;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setOnline(boolean online)
    {
        this.online= online;
    }

    public boolean isOnline()
    {
        return online;
    }
}