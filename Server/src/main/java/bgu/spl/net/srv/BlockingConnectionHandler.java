package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    // Specific handlers.
    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;

    // Communication.
    private final Socket sock;
    private Connections<T> connections;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile int connectionId; // ID given by the connections.

    // Internal use.
    private volatile boolean connected = true;

    // Synchronization.
    private Object lock;

    public BlockingConnectionHandler(Socket sock, Connections<T> connections, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.connections = connections;
        this.encdec = reader;
        this.protocol = protocol;
        this.connectionId = -1;
        this.lock = new Object();
    }
    
    @Override
    public void send(T msg) {
        
        synchronized (this.lock) { // TODO
    
            // We are not register yet \ Connection has closed.
            if (this.connectionId == -1) {
                System.out.println("Couldn't send message to client " + this);
                return;
            }
            
            // Send msg.
            try { 
                this.out.write(this.encdec.encode(msg));
                this.out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            // Synchronized this part in order not to get nessage to send while creating things.
            synchronized (this.lock) {
                // Create in&out buffers.
                this.in = new BufferedInputStream(sock.getInputStream());
                this.out = new BufferedOutputStream(sock.getOutputStream());
    
                // Register ourselves to connections.
                this.connectionId = this.connections.connect(this);
    
                // Activate our Protocol.
                this.protocol.start(this.connectionId, connections);
            }

            // Reading loop.
            while (!this.protocol.shouldTerminate() && this.connected && (read = this.in.read()) >= 0) {
                T nextMessage = this.encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    this.protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            synchronized (lock) {
                this.connectionId = -1;
            }
            ex.printStackTrace();
        }

        // Unregister.
        synchronized (lock) {
            this.connectionId = -1;
        }
        this.connections.disconnect(this.connectionId);
    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }
}
