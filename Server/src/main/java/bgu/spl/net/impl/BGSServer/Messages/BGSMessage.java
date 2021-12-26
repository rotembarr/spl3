package bgu.spl.net.impl.BGSServer.Messages;


public abstract class BGSMessage {

    public enum Opcode {
        NONE            {public byte[] toBytes() {return BGSMessage.shortToBytes((short)0);}}, 
        REGISTER        {public byte[] toBytes() {return BGSMessage.shortToBytes((short)1);}},
        LOGIN           {public byte[] toBytes() {return BGSMessage.shortToBytes((short)2);}},
        LOGOUT          {public byte[] toBytes() {return BGSMessage.shortToBytes((short)3);}},
        FOLLOW          {public byte[] toBytes() {return BGSMessage.shortToBytes((short)4);}},
        POST            {public byte[] toBytes() {return BGSMessage.shortToBytes((short)5);}},
        PM              {public byte[] toBytes() {return BGSMessage.shortToBytes((short)6);}},
        LOGSTAT         {public byte[] toBytes() {return BGSMessage.shortToBytes((short)7);}},
        STAT            {public byte[] toBytes() {return BGSMessage.shortToBytes((short)8);}},
        NOTIFICATION    {public byte[] toBytes() {return BGSMessage.shortToBytes((short)9);}},
        ACK             {public byte[] toBytes() {return BGSMessage.shortToBytes((short)10);}},
        ERROR           {public byte[] toBytes() {return BGSMessage.shortToBytes((short)11);}},
        BLOCK           {public byte[] toBytes() {return BGSMessage.shortToBytes((short)12);}}
    };

    Opcode opcode;
    public BGSMessage(Opcode opcode) {
        this.opcode = opcode;
    }

    public abstract String encode();

    public static byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public static short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
}
