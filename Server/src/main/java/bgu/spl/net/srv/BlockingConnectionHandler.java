package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.NotificationMessage;
import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    // Specific handlers.
    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;

    // Communication.
    private final Socket sock;
    private Connections<T> connections;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private AtomicInteger connectionId; // ID given by the connections.

    // Internal use.
    private volatile boolean connected = true;

    // Synchronization.
    private Object lock;

    public BlockingConnectionHandler(Socket sock, Connections<T> connections, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.connections = connections;
        this.encdec = reader;
        this.protocol = protocol;
        this.connectionId = new AtomicInteger(-1);
        this.lock = new Object();
    }
    
    @Override
    public void send(T msg) {
        
        synchronized (this.lock) { // TODO
    
            // We are not register yet \ Connection has closed.
            if (this.connectionId.get() == -1) {
                System.out.println("Couldn't send message to client " + this);
                return;
            }
            
            // Send msg.
            try { 
                this.out.write(this.encdec.encode(msg));
                this.out.flush();
            } catch (IOException ex) {
                this.close();
                ex.printStackTrace();
            }
        }
    }
    
    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            // Create in&out buffers.
            this.in = new BufferedInputStream(sock.getInputStream());
            this.out = new BufferedOutputStream(sock.getOutputStream());

            // Register ourselves to connections.
            this.connectionId.set(connections.connect(this));

            // Activate our Protocol. 
            // No way anyone send us message up to protocol will start, so no way of thread will insert between us.
            this.protocol.start(this.connectionId.get(), connections);

            // Reading loop.
            while (!this.protocol.shouldTerminate() && this.connected && (read = this.in.read()) >= 0) {
                T nextMessage = this.encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    this.protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            this.close();
            ex.printStackTrace();
        }
        
        // Unregister and close socket.
        this.close();
    }

    @Override
    public void close() {
        this.connected = false;
        this.connections.disconnect(this.connectionId.get());
        this.connectionId.set(-1);
        try {
            if (this.sock != null && this.sock.isConnected()) {
                this.sock.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
