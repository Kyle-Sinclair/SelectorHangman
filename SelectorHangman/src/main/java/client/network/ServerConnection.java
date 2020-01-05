package client.network;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;


import common.Message;
import common.MessageType;

/**
 * Manages all communication with the server.
 */
public class ServerConnection implements Runnable {
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    private boolean connected;
    private InetSocketAddress serverAddress;
    private SocketChannel socketChannel;
    private Selector selector;
    private OutputHandler outputHandler;
    private final Queue<Message> toBeSent = new ArrayDeque<Message>();
    private final ByteBuffer serverMessage = ByteBuffer.allocateDirect(8192);
    private final Queue<Message> toBeRead = new ArrayDeque<Message>();

    private volatile boolean timeToSend;

    private void establishConnection(SelectionKey key) throws IOException {
        this.socketChannel.finishConnect();
        outputHandler.handleMsg("Connection established");
        key.interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public void run() {
        try {
            initConnection();
            initSelector();

            while (connected) {

                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }

                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {

                    if (!key.isValid()) { continue; }
                    if (key.isConnectable()) { establishConnection(key); }
                    if (key.isReadable()) { readFromServer(key); }
                    if(key.isWritable()) {  writeToServer(key);}

                    selector.selectedKeys().remove(key);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //Read methods

    private void readFromServer(SelectionKey key) throws IOException {
        //System.out.println("added a message to the reading queue");

        serverMessage.clear();
        int numOfReadBytes = socketChannel.read(serverMessage);
        if (numOfReadBytes == -1) throw new IOException("Client has closed connection.");


        toBeRead.add(extractMessageFromBuffer());
            while(toBeRead.size() > 0){
                Message message = toBeRead.poll();
                outputHandler.handleMsg(message.getPayload().toString());
            }
    }

    private Message extractMessageFromBuffer() {
        serverMessage.flip();
        byte[] bytes = new byte[serverMessage.remaining()];
        serverMessage.get(bytes);
        String received = bytes.toString();
        //System.out.println("The following string has been extracted from the buffer: " + received + " which can be deserialised to " + Message.deserialize(bytes).toString());
        return Message.deserialize(bytes);
    }


    //Write methods

    public void enqueueAndSendMessage(MessageType messageType, String body) {
        Message message = new Message(messageType, body);

        synchronized (toBeSent) {
            toBeSent.add(message);
        }
        //ystem.out.println("The server connection has enqueued a message: " + message.toString());
        this.timeToSend = true;
        selector.wakeup();
    }

    public void writeToServer(SelectionKey key) throws IOException{
        synchronized (toBeSent) {
            while (toBeSent.size() > 0) {
                ByteBuffer message = ByteBuffer.wrap(Message.serialize(toBeSent.poll()).getBytes());
                socketChannel.write(message);
                //System.out.println("The server connection has written to the server");

            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }


    public void connect(String host, int port, OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
        this.serverAddress = new InetSocketAddress(host,port);
        new Thread(this).start();
    }


    // Initalization methods

    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    private void initConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;
    }


    public void setListener(OutputHandler output) {
        this.outputHandler = output;
    }

    public void disconnect() {
    }
}
