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
    
    public RegisterMessage(String username, String password, String birthday) {
        super(BGSMessage.Opcode.REGISTER);
        this.username = username;
        this.password = password;
        this.birthday = birthday;
    }
    
    public static RegisterMessage decode(String src) {
        String[] parts = src.split("\0");
        return new RegisterMessage(parts[0], parts[1], parts[2]);
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + this.username + '\0' + this.password + '\0' + this.birthday + '\0';
    }

    public String toString() {
        return "RegisterMessage(" + this.hashCode() + "): " +  this.username + " " + this.password + " " + this.birthday + " ";
    }
} 
