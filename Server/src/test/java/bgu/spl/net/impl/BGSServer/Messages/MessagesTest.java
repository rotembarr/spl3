package bgu.spl.net.impl.BGSServer.Messages;


import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import bgu.spl.net.impl.BGSServer.Filter;

public class MessagesTest {
    
    @Test
    public void TestRegisterData() {
        String src = "aaa" + '\0' + "bbb" + '\0' + "ccc" + '\0';
        String[] parts = src.split("\0");
        System.out.println(parts.length);
        for (String part : parts) {
            System.out.println(part);
        }
    }

    @Test
    public void TestSocket() {
        Thread serverThread = new Thread() {
            @Override
            public void run() {
                try (ServerSocket socket = new ServerSocket(7777)) {
                    Socket client = socket.accept();
                    BufferedInputStream in = new BufferedInputStream(client.getInputStream());
                    System.out.println("server got: " + in.read());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("server finish");
            }
        };
        serverThread.start();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try (Socket socket = new Socket("10.0.2.15", 7777)) {
            Thread readThread = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                        System.out.println(in.read());
                    } catch (IOException e) {
                        System.out.println("read exception");
                        e.printStackTrace();
                    }
                    System.out.println("read finish");
                }
            };

            Thread writeThread = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                        Thread.sleep(10);
                        byte[] b = "a".getBytes();
                        out.write(b);
                        out.flush();
                    } catch (IOException | InterruptedException e) {
                        System.out.println("write exception");
                        e.printStackTrace();
                    }
                    System.out.println("write finish");
                }
            };
            

            readThread.start();
            writeThread.start();
            writeThread.join();
            serverThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    @Test
    public void testRegisterParsing() {
        String s = "aaa" + '\0' + "bbb" + '\0' + "ccc" + '\0';
        RegisterMessage msg = RegisterMessage.decode(s);
        assertEquals("aaa", msg.getUsername());
        assertEquals("bbb", msg.getPassword());
        assertEquals("ccc", msg.getBirthday());
    }

    @Test
    public void testloginParsing() {
        byte[] charset = {7};
        String s = "aaa" + '\0' + "bbb" + '\0' + new String(charset, StandardCharsets.UTF_8);
        LoginMessage msg = LoginMessage.decode(s);
        assertEquals("aaa", msg.getUsername());
        assertEquals("bbb", msg.getPassword());
        assertEquals(7, msg.getCaptcha());
    }


    @Test
    public void testFollowParsing() {
        byte[] follow = {1};
        String s = new String(follow, StandardCharsets.UTF_8) +  "aaa" + '\0';
        FollowMessage msg = FollowMessage.decode(s);
        assertEquals(1, msg.getFollow());
        assertEquals("aaa", msg.getUsername());
    }

    @Test
    public void testd() {
        String[] arr = "@dsdsf asdasda dsvsdv sd @sf asd @sds dssdf @waas".split(" ");
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].charAt(0) ==  '@') {
                System.out.println(arr[i].substring(1));
            }
        }

        String text = "as ds a Kaki dv@ dsvd !@# Kakidss sdsaa daspipi  sv ";
        System.out.println(Filter.filter(text));
        
        short a = 49;
        String s = new String(BGSMessage.shortToBytes(a), StandardCharsets.UTF_8);
        System.out.println(s);
        System.out.println(s.length());

        String d = "11";
        int k = Integer.parseInt(d);
        System.out.println(k);

    }

    @Test
    public void testNotificationEncode() {
        String username = "Rotem Bar!";
        String content = "asd asd @asd @@";
        NotificationMessage msg = new NotificationMessage((byte)1, username,content);
        String s = msg.encode();
        byte[] arr = s.getBytes();
        String s2 = new String(arr, StandardCharsets.UTF_8);
        assertEquals(s, s2);

        System.out.println(s);
    }
}