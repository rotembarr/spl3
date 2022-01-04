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
    
    public LoginMessage(String username, String password, byte captcha) {
        super(BGSMessage.Opcode.LOGIN);
        this.username = username;
        this.password = password;
        this.captcha = captcha;
    }
    
    public static LoginMessage decode(String src) {
        String[] parts = src.split("\0");
        return new LoginMessage(parts[0], parts[1], (byte)src.charAt(src.length()-1));
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.username + '\0' + this.password + '\0' + this.captcha;
    }

    public String toString() {
        return "Login(" + this.hashCode() + "): " + this.username + " " + this.password + " " + this.captcha;
    }
} 
