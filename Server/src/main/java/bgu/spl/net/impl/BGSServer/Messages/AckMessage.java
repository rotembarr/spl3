package bgu.spl.net.impl.BGSServer.Messages;

public class AckMessage extends BGSMessage{
    private final BGSMessage.Opcode messageOpcode;
    private final String optional;

    public BGSMessage.Opcode getMessageOpcode() {
        return this.messageOpcode;
    }

    public String getOptional() {
        return this.optional;
    }
    
    public AckMessage(BGSMessage.Opcode messageOpcode, String optional) {
        super(BGSMessage.Opcode.ACK);
        this.messageOpcode = messageOpcode;
        this.optional = optional;
    }

    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + BGSMessage.opcodeToString(this.messageOpcode) + this.optional;
    }

} 
