package bgu.spl.net.impl.BGSServer.Messages;

public class AckMessage extends BGSMessage{
    private final BGSMessage.Opcode messageOpcode;
    private final String optional;

    public AckMessage(BGSMessage.Opcode messageOpcode, String optional) {
        super(BGSMessage.Opcode.ACK);
        this.messageOpcode = messageOpcode;
        this.optional = optional;
    }

    public boolean equals(Object other) {
        if (other instanceof AckMessage) {
            return (this.messageOpcode == ((AckMessage)other).messageOpcode && 
                this.optional.equals(((AckMessage)other).optional)
            );
        } else {
            return false;
        }
    }
    
    public BGSMessage.Opcode getMessageOpcode() {
        return this.messageOpcode;
    }

    public String getOptional() {
        return this.optional;
    }
    
    public String encode() {
        return BGSMessage.opcodeToString(this.opcode) + BGSMessage.opcodeToString(this.messageOpcode) + this.optional;
    }

    public String toString() {
        return this.opcode + " " + this.messageOpcode + " " + this.optional;
    }

} 
