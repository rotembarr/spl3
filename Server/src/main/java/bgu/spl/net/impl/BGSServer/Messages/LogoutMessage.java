package bgu.spl.net.impl.BGSServer.Messages;

public class LogoutMessage extends BGSMessage{
    
    public LogoutMessage(String src) {
        super(BGSMessage.Opcode.LOGOUT);
    }


    public String encode() {
        return null;
    }

    public String toString() {
        return "Logout(" + this.hashCode() + ")";
    }
} 

