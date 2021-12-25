package bgu.spl.net.impl.BGSServer.Messages;

public class LoginMessage extends BaseMessage{
    String username;
    String password;
    String captcha;
    
    public LoginMessage(String src) {
        String[] parts = src.split("\0");
        this.username = parts[0];
        this.password = parts[1];
        this.captcha = parts[2];
    }

    public String toString() {
        return this.username + " " + this.password + " " + this.captcha;
    }
} 
