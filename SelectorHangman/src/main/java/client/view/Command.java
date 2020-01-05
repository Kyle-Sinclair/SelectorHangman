package client.view;


public enum Command {

    /**
     * Establish a connection to the server.
     */
    CONNECT,
    /**
     * Leave the game. Shuts down the socket and cancels the thread handlers
     */
    QUIT,
    /**
     * What did you do? Why is it like this?
     */
    NO_COMMAND,
    /**
     * Triggers the server to make a new game model
     */
    START,
    /**
     * Provides a string as a guess to the server. The processing
     * of whether the guess is a letter, a word or correct is done
     * on the server side
     */
    GUESS,
}
