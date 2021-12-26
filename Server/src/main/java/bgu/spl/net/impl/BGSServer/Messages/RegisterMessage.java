package bgu.spl.net.impl.BGSServer.Messages;

public class RegisterMessage extends BGSMessage{
    String username;
    String password;
    String birthday;
    
    public RegisterMessage(String src) {
        super(BGSMessage.Opcode.REGISTER);
        String[] parts = src.split("\0");
        this.username = parts[0];
        this.password = parts[1];
        this.birthday = parts[2];
    }

    public String encode() {
        return null;
    }

    public String toString() {
        return "RegisterMessage(" + this.hashCode() + "): " +  this.username + " " + this.password + " " + this.birthday + " ";
    }
} 
