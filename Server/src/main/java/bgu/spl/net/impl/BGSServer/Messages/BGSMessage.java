package bgu.spl.net.impl.BGSServer.Messages;


public abstract class BGSMessage {

    public enum Opcode {
        NONE,
        REGISTER,
        LOGIN,
        LOGOUT,
        FOLLOW,
        POST,
        PM,
        LOGSTAT,
        STAT,
        NOTIFICATION,
        ACK,
        ERROR,
        BLOCK
    };

    Opcode opcode;
    public BGSMessage(Opcode opcode) {
        this.opcode = opcode;
    }

    public abstract String encode();

    public static String opcodeToString(Opcode opcode) {
        String s;
        if (opcode == Opcode.REGISTER) {
            s = new String(BGSMessage.shortToBytes((short)1));
        } else if (opcode == Opcode.LOGIN) {
            s = new String(BGSMessage.shortToBytes((short)2));
        } else if (opcode == Opcode.LOGOUT) {
            s = new String(BGSMessage.shortToBytes((short)3));
        } else if (opcode == Opcode.FOLLOW) {
            s = new String(BGSMessage.shortToBytes((short)4));
        } else if (opcode == Opcode.POST) {
            s = new String(BGSMessage.shortToBytes((short)5));
        } else if (opcode == Opcode.PM) {
            s = new String(BGSMessage.shortToBytes((short)6));
        } else if (opcode == Opcode.LOGSTAT) {
            s = new String(BGSMessage.shortToBytes((short)7));
        } else if (opcode == Opcode.STAT) {
            s = new String(BGSMessage.shortToBytes((short)8));
        } else if (opcode == Opcode.NOTIFICATION) {
            s = new String(BGSMessage.shortToBytes((short)9));
        } else if (opcode == Opcode.ACK) {
            s = new String(BGSMessage.shortToBytes((short)10));
        } else if (opcode == Opcode.ERROR) {
            s = new String(BGSMessage.shortToBytes((short)11));
        } else if (opcode == Opcode.BLOCK) {
            s = new String(BGSMessage.shortToBytes((short)12));    
        } else {
            s= null;
        }
        return s;
    }

    public static Opcode stringToOpcode(String s) {
        short opcode = BGSMessage.bytesToShort(s.getBytes());
        if (opcode == 1) {
            return Opcode.REGISTER;
        } else if (opcode == 2) {
            return Opcode.LOGIN;
        } else if (opcode == 3) {
            return Opcode.LOGOUT;
        } else if (opcode == 4) {
            return Opcode.FOLLOW;
        } else if (opcode == 5) {
            return Opcode.POST;
        } else if (opcode == 6) {
            return Opcode.PM;
        } else if (opcode == 7) {
            return Opcode.LOGSTAT;
        } else if (opcode == 8) {
            return Opcode.STAT;
        } else if (opcode == 9) {
            return Opcode.NOTIFICATION;
        } else if (opcode == 10) {
            return Opcode.ACK;
        } else if (opcode == 11) {
            return Opcode.ERROR;
        } else if (opcode == 12) {
            return Opcode.BLOCK;
        } else {
            return Opcode.NONE;
        }
}

    public static byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public static short bytesToShort(byte[] byteArr) {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
}
