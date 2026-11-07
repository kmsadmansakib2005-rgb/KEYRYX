package security;

public class Session {
    public enum Role {
        USER,
        ADMIN
    }
    private final String token;
    private final String username;
    private final Role role;
    private final long createdAt;

    private volatile long lastActivity;

    Session(String token,String username, Role role)
    {
        this.token = token;
        this.username= username;
        this.role= role;

        this.createdAt=System.currentTimeMillis();
        this.lastActivity= createdAt;
    }

    public String getToken() {
        return token;
    }

    public String getUsername()
    {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public long getCreatedAt()
    {
        return createdAt;
    }

    long getLastActivity()
    {
        return lastActivity;
    }

    void updateActivity()
    {
        lastActivity= System.currentTimeMillis();
    }
}