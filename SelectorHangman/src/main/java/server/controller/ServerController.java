package server.controller;

import common.*;
import server.model.Game;

public class ServerController {

    Game game;

    public ServerController() {
        game = new Game();
    }


    public Object getGameState() {

        //TODO: return a game snap shot
        return null;
    }

    public Message guess(String guess) {
        return game.makeAGuess(guess);
    }

}
