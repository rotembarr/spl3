package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Server;

import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPCMain {

    public static void main(String[] args) {

        // Create a map to all users.
        Map<String, BGSStudent> usernamesToIdMap = new ConcurrentHashMap<String, BGSStudent>();

        if (args.length != 1) {
            System.out.println("Bad parameters passes. Usage <port> <Num of threads>");
        }

       Server.<BGSMessage>threadPerClient(
            Integer.parseInt(args[0]),
            () -> new BGSProtocol(usernamesToIdMap), //protocol factory
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
