package bgu.spl.net.impl.BGSServer.Messages;

public class LoginMessage extends BGSMessage{
    String username;
    String password;
    String captcha;
    
    public LoginMessage(String src) {
        super(BGSMessage.Opcode.LOGIN);
        String[] parts = src.split("\0");
        this.username = parts[0];
        this.password = parts[1];
        this.captcha = parts[2];
    }

    public String encode() {
        return null;
    }

    public String toString() {
        return this.username + " " + this.password + " " + this.captcha;
    }
} 
