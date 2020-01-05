package server.net;


import java.io.IOException;
import java.net.Socket;

import server.controller.ServerController;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import common.*;
import server.model.Game;

import static java.lang.Thread.sleep;


public class ClientHandler implements Runnable {

    private  Socket clientSocket;
    private ServerController controller;
    private boolean connected;
    private final Queue<Message> toBeRead = new ArrayDeque<Message>();
    private final Queue<Message> toBeSent = new ArrayDeque<Message>();
    private final ByteBuffer clientMessage = ByteBuffer.allocateDirect(8192);
    private GameServer server;
    SocketChannel channel;
    public SelectionKey selectionKey;
    Game game;
    public static final int MS_LINGER = 18000000;
    private boolean timeToSend;


    public ClientHandler(GameServer server, SocketChannel channel){
        this.server = server;
        this.channel = channel;
        game = new Game();
    }

    @Override
    public void run() {
        Iterator<Message> iterator = toBeRead.iterator();
        while (iterator.hasNext())
            {
            Message message = iterator.next();
                //System.out.println("The client handler has received the message " + message.toString());

                Message response;
                switch (message.type) {

                    case START:
                        response = game.startNewGame();
                        if(response == null){
                            System.out.println("The start response message is null");
                        }
                            addMessageToWritingQueue(response);
                        break;
                    case GUESS:

                        if(message.payload.toString().length() == 1){ response = game.makeAGuess(message.getPayload().toString().charAt(0)); }
                        else { response = game.makeAGuess(message.payload.toString()); }

                        addMessageToWritingQueue(response);
                        break;
                    case DISCONNECT:
                        disconnect();
                        break;
                    default:
                        throw new MessageException("Received corrupt message: ");
                }
                iterator.remove();
            }
        }

    public void addMessageToWritingQueue(Message message) {
        synchronized (toBeSent) {
            toBeSent.add(message);
            //System.out.println("The message " + message + "has been added to the writing queue");

        }
        server.addKeyToWritingQueue(this.selectionKey);
        server.wakeupSelector();
    }


    public void sendMessages() throws IOException {
        synchronized (toBeSent) {
            while (toBeSent.size() > 0) {

                ByteBuffer message = ByteBuffer.wrap(Message.serialize(toBeSent.poll()).getBytes());
                byte[] arr = new byte[message.remaining()];

                channel.write(message);
                //System.out.println("A message has been sent to the client ");

            }
        }
    }


    void readMessage() throws IOException {
        clientMessage.clear();
        int numOfReadBytes = channel.read(clientMessage);
        if (numOfReadBytes == -1) throw new IOException("Client has closed connection.");
        Message received = extractMessageFromBuffer();
        //System.out.println(" The message " + received.toString() + "has been dequeued");
        toBeRead.add(received);
        ForkJoinPool.commonPool().execute(this);
    }


    private Message extractMessageFromBuffer(){
        clientMessage.flip();
        byte[] bytes = new byte[clientMessage.remaining()];
        clientMessage.get(bytes);
        String received = bytes.toString();
        //System.out.println("The following string has been extracted from the buffer: " + received + " which can be deserialised to " + Message.deserialize(bytes).toString());
        return Message.deserialize(bytes);
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }


    void disconnect() {
        try {
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        connected = false;
    }
}
