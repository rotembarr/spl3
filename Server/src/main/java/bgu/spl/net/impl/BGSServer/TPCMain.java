package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Server;

import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.BGSEncoderDecoder;
import bgu.spl.net.impl.BGSServer.BGSProtocol;

public class TPCMain {

    public static void main(String[] args) {

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
