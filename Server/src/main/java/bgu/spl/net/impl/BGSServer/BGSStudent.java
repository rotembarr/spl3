package bgu.spl.net.impl.BGSServer;

public class BGSStudent {
    private String username;
    private String password;
    private String birthday;
    private int connectionId;
    private boolean connected;
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }

    public String getBirthday() {
        return this.birthday;
    }
    
    public int getConnectionId() {
        return this.connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public BGSStudent(String username, String password, String birthday) {
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        this.connectionId = -1;
        this.connected = false;
    }
    
}
