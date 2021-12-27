package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.ThreadPerClient.BlockingConnections;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    // Messaging.
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private Connections<T> connections;

    // Socket.
    private final int port;
    private ServerSocket sock;

    public BaseServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
        this.connections = new BlockingConnections<T>();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started on port " + port);

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                // Accept new client.
                Socket clientSock = serverSock.accept();
                System.out.println("Server accept");

                // Create message handlers.
                MessageEncoderDecoder<T> encdec = encdecFactory.get();
                BidiMessagingProtocol<T> protocol = protocolFactory.get();

                // Run Connection-Handler!!!
                // Here is a user define way of running.
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<T>(
                        clientSock,
                        this.connections,
                        encdec,
                        protocol);

                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
