package bgu.spl.net.impl.BGSServer.Messages;

public class LogoutMessage extends BGSMessage{
    
    public LogoutMessage() {
        super(BGSMessage.Opcode.LOGOUT);
    }

    public static LogoutMessage decode(String src) {
        return new LogoutMessage();
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode);
    }

    public String toString() {
        return "Logout(" + this.hashCode() + ")";
    }
} 

