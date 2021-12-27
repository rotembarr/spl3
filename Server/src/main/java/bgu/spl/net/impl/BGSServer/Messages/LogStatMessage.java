package bgu.spl.net.impl.BGSServer.Messages;

public class LogStatMessage extends BGSMessage{
    
    public LogStatMessage(String src) {
        super(BGSMessage.Opcode.LOGSTAT);
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode);
    }

    public String toString() {
        return "LogStat(" + this.hashCode() + ")";
    }
} 

