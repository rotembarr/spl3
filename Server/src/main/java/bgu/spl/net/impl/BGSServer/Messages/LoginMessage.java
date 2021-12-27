package bgu.spl.net.impl.BGSServer.Messages;

public class LoginMessage extends BGSMessage{
    private String username;
    private String password;
    private byte captcha;

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public byte getCaptcha() {
        return this.captcha;
    }
    
    public LoginMessage(String src) {
        super(BGSMessage.Opcode.LOGIN);
        String[] parts = src.split("\0");
        this.username = parts[0];
        this.password = parts[1];
        this.captcha = (byte)parts[2].charAt(0);
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.username + '\0' + this.password + '\0' + this.captcha;
    }

    public String toString() {
        return "Login(" + this.hashCode() + "): " + this.username + " " + this.password + " " + this.captcha;
    }
} 
