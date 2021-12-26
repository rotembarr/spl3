package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
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
    private BufferedInputStream in;
    private BufferedOutputStream out;

    // Internal use.
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }
    @Override
    public void send(T msg) {
        synchronized (this.out) { // TODO
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

            this.in = new BufferedInputStream(sock.getInputStream());
            this.out = new BufferedOutputStream(sock.getOutputStream());

            while (!this.protocol.shouldTerminate() && this.connected && (read = this.in.read()) >= 0) {
                T nextMessage = this.encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    this.protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }
}
