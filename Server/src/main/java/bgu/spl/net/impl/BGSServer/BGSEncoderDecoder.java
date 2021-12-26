package bgu.spl.net.impl.BGSServer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.Messages.FollowMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogStatMessage;
import bgu.spl.net.impl.BGSServer.Messages.LoginMessage;
import bgu.spl.net.impl.BGSServer.Messages.LogoutMessage;
import bgu.spl.net.impl.BGSServer.Messages.NotificationMessage;
import bgu.spl.net.impl.BGSServer.Messages.PMMessage;
import bgu.spl.net.impl.BGSServer.Messages.PostMessage;
import bgu.spl.net.impl.BGSServer.Messages.RegisterMessage;
import bgu.spl.net.impl.BGSServer.Messages.StatMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage.Opcode;

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
            String buffer = new String(this.bytes, StandardCharsets.UTF_8);
            buffer = buffer.substring(2); // Remove opcode
            
            // bytes[0]
            // bytes[1]
            
            // Create message accordin to opcode.
            // TODO mor: understand how opcode is passing, and correct this code.
            BGSMessage.Opcode opcode = Opcode.NONE; //TODO - here is the changing
            if (opcode == BGSMessage.Opcode.REGISTER) {
                return new RegisterMessage(buffer);
            } else if (opcode == BGSMessage.Opcode.LOGIN) {
                return new LoginMessage(buffer);
            } else if (opcode == BGSMessage.Opcode.LOGOUT) {
                return new LogoutMessage(buffer);
            } else if (opcode == BGSMessage.Opcode.FOLLOW) {
                return new FollowMessage(buffer);
            } else if (opcode == BGSMessage.Opcode.POST) {
                return new PostMessage(buffer);
            } else if (opcode == BGSMessage.Opcode.PM) {
                return new PMMessage(buffer);
            } else if (opcode == BGSMessage.Opcode.LOGSTAT) {
                return new LogStatMessage(buffer);
            } else if (opcode == BGSMessage.Opcode.STAT) {
                return new StatMessage(buffer);
            } else if (opcode == BGSMessage.Opcode.NOTIFICATION) {
                return new NotificationMessage(buffer);
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
        return (message.toString() + ";").getBytes(); //uses utf8 by default
        // TODO change to encode
    }

}
