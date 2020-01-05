package client.startup;

import client.view.NonBlockingInterpreter;

public class StartUp {

    public static void main(String[] args) {
        new NonBlockingInterpreter().start();
    }
}
