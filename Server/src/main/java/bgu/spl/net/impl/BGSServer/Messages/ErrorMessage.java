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

    public boolean equals(Object other) {
        if (other instanceof ErrorMessage) {
            return (this.opcode == ((ErrorMessage)other).opcode) && (this.messageOpcode == ((ErrorMessage)other).messageOpcode);
        } else {
            return false;
        }
    }


    public String encode() {
        return "Error(" + this.hashCode() + "): " + BGSMessage.opcodeToString(this.opcode) + BGSMessage.opcodeToString(this.messageOpcode);
    }

} 
