package server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.controller.ServerController;
import common.Definitions;
import server.model.Game;

import java.util.ArrayDeque;
import java.util.Queue;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;



public class GameServer {
    private static final int LINGER_TIME = 5000;
    private static final int TIMEOUT_HALF_HOUR = 1800000;

    private int portNo = 8080;
    static ServerSocket socket;
    private Queue<SelectionKey> writableKeys = new ArrayDeque<>();
    private Selector selector;
    private ServerSocketChannel listeningSocketChannel;

    public GameServer() throws IOException {

        this.selector = initSelector();

    }

    public static void main(String[] args) throws IOException {
        try {
            GameServer.inititalize();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GameServer server = new GameServer();

        System.out.println("Server beginning service");
        server.serve();
    }

    private void serve() {
        while (true){
            try {


                setWritableKeys();
                this.selector.select();
                for (SelectionKey key : this.selector.selectedKeys()){

                    if(key.isReadable()) {  readFromClient(key); }
                    else if(key.isWritable())   { writeToKey(key); }
                    else if(key.isAcceptable()) { startHandler(key); }


                    selector.selectedKeys().remove(key);
                }
            } catch (IOException ex) {
                Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setWritableKeys() {
        while(!writableKeys.isEmpty()){
            writableKeys.poll().interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void writeToKey(SelectionKey key) throws IOException {
        ClientHandler clientHandler = (ClientHandler) key.attachment();
        clientHandler.sendMessages();
        key.interestOps(SelectionKey.OP_READ);
    }

    private void startHandler(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);

        ClientHandler handler = new ClientHandler(this, clientChannel);
        SelectionKey selectionKey = clientChannel.register(selector, SelectionKey.OP_READ, handler);
        handler.setSelectionKey(selectionKey);
        System.out.println("A client handler with selector key has been started");

    }

    private void readFromClient(SelectionKey key) throws IOException {
        ClientHandler clientHandler = (ClientHandler) key.attachment();
        try {
            clientHandler.readMessage();
        } catch (IOException e) {
            removeClient(key);
        }
        key.interestOps(SelectionKey.OP_WRITE);

    }

    private Selector initSelector() throws IOException {
        Selector socketSelector = SelectorProvider.provider().openSelector();

        this.listeningSocketChannel = ServerSocketChannel.open();
        this.listeningSocketChannel.configureBlocking(false);
        this.listeningSocketChannel.socket().bind(new InetSocketAddress(8081));
        this.listeningSocketChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    public static void inititalize() throws IOException {
            socket = new ServerSocket(Definitions.PORT);
            Game.initializeDictionary("C:\\Users\\Joint Account\\Documents\\Hangman\\src\\main\\java\\server\\model\\words.txt");
        }

    private void removeClient(SelectionKey key) throws IOException {
        ClientHandler clientHandler = (ClientHandler) key.attachment();
        //clientHandler.disconnectClient();
        key.cancel();
    }

    public void wakeupSelector() {
        selector.wakeup();
    }

    public void addKeyToWritingQueue(SelectionKey writableKey) {
        if(writableKey == null){
            System.out.println("The selection key is null");
        }
        writableKeys.add(writableKey);
    }
}
