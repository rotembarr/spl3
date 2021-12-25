package bgu.spl.net.impl.BGSServer.Messages;

public class RegisterMessage extends BaseMessage{
    String username;
    String password;
    String birthday;
    
    public RegisterMessage(String src) {
        String[] parts = src.split("\0");
        this.username = parts[0];
        this.password = parts[1];
        this.birthday = parts[2];
    }

    public String toString() {
        return this.username + " " + this.password + " " + this.birthday + " ";
    }
} 
