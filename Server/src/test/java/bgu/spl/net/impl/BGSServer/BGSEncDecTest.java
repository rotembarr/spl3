package bgu.spl.net.impl.BGSServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.Messages.PMMessage;
import bgu.spl.net.impl.BGSServer.Messages.PostMessage;
import bgu.spl.net.impl.BGSServer.Messages.BGSMessage.Opcode;

public class BGSEncDecTest {
    BGSEncoderDecoder encdec;

    public BGSEncDecTest() {
        this.encdec = new BGSEncoderDecoder();
    }
    

    @Test
    public void testPost() {
        String content = "@ asdas ad Kaki asfda asddas @s dvsd";
        String src = BGSMessage.opcodeToString(Opcode.POST) + content + '\0' + ';';
        
        byte[] arr = src.getBytes();
        for (int i = 0; i < arr.length - 1; i++) {
            assertEquals(null, this.encdec.decodeNextByte(arr[i]));
        }
        BGSMessage msg = this.encdec.decodeNextByte(arr[arr.length-1]);
        assertTrue("Bad type", msg instanceof PostMessage);
        PostMessage castmsg = (PostMessage)msg;
        assertEquals(content, castmsg.getContent());
    }


    @Test
    public void testPM() {
        String username = "asd sdfasd  dad";
        String dateandtime = "11-5-2222";
        String content = "@ asdas ad Kaki asfda asddas @s dvsd";
        String src = BGSMessage.opcodeToString(Opcode.PM) + username + '\0' + content + '\0' + dateandtime + '\0' + ';';
        
        byte[] arr = src.getBytes();
        for (int i = 0; i < arr.length - 1; i++) {
            assertEquals(null, this.encdec.decodeNextByte(arr[i]));
        }
        BGSMessage msg = this.encdec.decodeNextByte(arr[arr.length-1]);
        assertTrue("Bad type", msg instanceof PMMessage);
        PMMessage castmsg = (PMMessage)msg;
        assertEquals(username, castmsg.getUsername());
        assertEquals(content, castmsg.getContent());
        assertEquals(dateandtime, castmsg.getSendingDateAndTime());
    }

}


