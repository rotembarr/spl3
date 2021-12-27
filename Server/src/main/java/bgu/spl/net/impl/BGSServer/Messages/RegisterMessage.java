package bgu.spl.net.impl.BGSServer.Messages;

public class RegisterMessage extends BGSMessage{
    private String username;
    private String password;
    private String birthday;

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getBirthday() {
        return this.birthday;
    }
    
    public RegisterMessage(String src) {
        super(BGSMessage.Opcode.REGISTER);
        String[] parts = src.split("\0");
        this.username = parts[0];
        this.password = parts[1];
        this.birthday = parts[2];
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.username + '\0' + this.password + '\0' + this.birthday + '\0';
    }

    public String toString() {
        return "RegisterMessage(" + this.hashCode() + "): " +  this.username + " " + this.password + " " + this.birthday + " ";
    }
} 
