package model;

public class user {

    private String userName;
    private boolean online;

    public user(String userName, boolean online)
    {
        this.userName= userName;
        online= true;
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