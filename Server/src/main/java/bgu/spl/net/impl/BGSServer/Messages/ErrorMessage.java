package bgu.spl.net.impl.BGSServer.Messages;

public class ErrorMessage extends BGSMessage{
    private final BGSMessage.Opcode messageOpcode;

    public Object getMessageOpcode() {
        return this.messageOpcode;
    }

    public ErrorMessage(BGSMessage.Opcode messageOpcode) {
        super(BGSMessage.Opcode.ACK);
        this.messageOpcode = messageOpcode;
    }

    public String encode() {
        return "Error(" + this.hashCode() + "): " + BGSMessage.opcodeToString(this.opcode) + BGSMessage.opcodeToString(this.messageOpcode);
    }

} 
