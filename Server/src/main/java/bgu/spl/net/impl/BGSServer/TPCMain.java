package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Server;

import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.BGSEncoderDecoder;
import bgu.spl.net.impl.BGSServer.BGSProtocol;

public class TPCMain {

    public static void main(String[] args) {
        Connections<BGSMessage> connections; // TODO

        if (args.length != 1) {
            System.out.println("Bad parameters passes. Usage <port> <Num of threads>");
        }

       Server.<BGSMessage>threadPerClient(
            Integer.parseInt(args[0]),
            () -> new BGSProtocol(), //protocol factory
            () -> new BGSEncoderDecoder() //message encoder decoder factory
       ).serve();
    }
}



// try (ServerSocket socket = new ServerSocket(7777)) {
//     int read;
//     byte[] buffer = new byte[100];
//     int index = 0;

//     System.out.println("accepting");
//     Socket client = socket.accept(); 
//     BufferedInputStream in = new BufferedInputStream(client.getInputStream());

//     while ((read = in.read()) >= 0) {
//         buffer[index] = (byte)read;
//         index++;
//         System.out.println("server got: " + read + " " + new String(buffer, StandardCharsets.UTF_8));
//     }
// } catch (IOException e) {
//     e.printStackTrace();
// }
// System.out.println("server finish");
