package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Server;

import bgu.spl.net.impl.BGSServer.Messages.BGSMessage;
import bgu.spl.net.impl.BGSServer.BGSEncoderDecoder;
import bgu.spl.net.impl.BGSServer.BGSProtocol;

public class ReactorMain {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Bad parameters passes. Usage <port> <Num of threads>");
        }


        
        // TODO - refactor this
        // Server.<BGSMessage>reactor(
        //     Integer.parseInt(args[1]), // nThreads
        //     Integer.parseInt(args[0]), //port
        //     () -> new BGSProtocol(), //protocol factory
        //     () -> new BGSEncoderDecoder() //message encoder decoder factory
        // ).serve();

    }
}
