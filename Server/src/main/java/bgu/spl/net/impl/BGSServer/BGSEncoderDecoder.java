package bgu.spl.net.impl.BGSServer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.Messages.BlockMessage;
import bgu.spl.net.impl.BGSServer.Messages.FollowMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogStatMessage;
import bgu.spl.net.impl.BGSServer.Messages.LoginMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogoutMessage;
import bgu.spl.net.impl.BGSServer.Messages.PMMessage;
import bgu.spl.net.impl.BGSServer.Messages.PostMessage;
import bgu.spl.net.impl.BGSServer.Messages.RegisterMessage;
import bgu.spl.net.impl.BGSServer.Messages.StatMessage;

public class BGSEncoderDecoder implements MessageEncoderDecoder<BGSMessage> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public BGSMessage decodeNextByte(byte nextByte) {
        // Notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        // This allow us to do the following comparison:
        if (nextByte == ';') {
            
            // Not a real message.
            if (this.len < 2) {
                return null;
            }

            // Parse input message from decimal to UTF-8.
            String buffer = new String(Arrays.copyOfRange(this.bytes, 0, len), StandardCharsets.UTF_8);
            BGSMessage.Opcode opcode = BGSMessage.stringToOpcode(buffer.substring(0,2));
            buffer = buffer.substring(2); // Remove opcode
            
            // Prepare new msg (before returning a msg).
            this.len = 0;

            // Create message accordin to opcode.
            if (opcode == BGSMessage.Opcode.REGISTER) {
                return RegisterMessage.decode(buffer);
            } else if (opcode == BGSMessage.Opcode.LOGIN) {
                return LoginMessage.decode(buffer);
            } else if (opcode == BGSMessage.Opcode.LOGOUT) {
                return LogoutMessage.decode(buffer);
            } else if (opcode == BGSMessage.Opcode.FOLLOW) {
                return FollowMessage.decode(buffer);
            } else if (opcode == BGSMessage.Opcode.POST) {
                return PostMessage.decode(buffer);
            } else if (opcode == BGSMessage.Opcode.PM) {
                return PMMessage.decode(buffer);
            } else if (opcode == BGSMessage.Opcode.LOGSTAT) {
                return LogStatMessage.decode(buffer);
            } else if (opcode == BGSMessage.Opcode.STAT) {
                return StatMessage.decode(buffer);
            } else if (opcode == BGSMessage.Opcode.BLOCK) {
                return BlockMessage.decode(buffer);
            } else {
                System.out.println("Error parsing enc dec");
            }

        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }


    @Override
    public byte[] encode(BGSMessage message) {
        return (message.encode() + ";").getBytes(); //uses utf8 by default
    }

}
