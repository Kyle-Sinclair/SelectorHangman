package client.controller;


import java.io.IOException;
import java.io.UncheckedIOException;

import java.util.concurrent.CompletableFuture;
import client.network.ServerConnection;
import client.network.OutputHandler;
import client.view.NonBlockingInterpreter;
import common.MessageType;


public class ClientController {
    private final ServerConnection serverConnection = new ServerConnection();


    public void connect(String host, int port, OutputHandler outputHandler) {
        serverConnection.connect(host,port,outputHandler);
    }

    public void guess(String guess) throws IOException {
        serverConnection.enqueueAndSendMessage(MessageType.GUESS, guess);
    }
    public void startNewGame() {
        serverConnection.enqueueAndSendMessage(MessageType.START,"");

    }

    public void disconnect() {
        serverConnection.disconnect();
    }

}
