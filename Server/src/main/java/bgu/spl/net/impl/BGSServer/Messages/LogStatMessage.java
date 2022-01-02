package bgu.spl.net.impl.BGSServer.Messages;

public class LogStatMessage extends BGSMessage{
    
    public LogStatMessage() {
        super(BGSMessage.Opcode.LOGSTAT);
    }

    public static LogStatMessage decode(String src) {
        return new LogStatMessage();
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode);
    }

    public String toString() {
        return "LogStat(" + this.hashCode() + ")";
    }
} 

