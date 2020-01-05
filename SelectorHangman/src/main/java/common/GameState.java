package common;
import java.io.Serializable;

public class GameState implements Serializable {

    public final int points;
    public final int lives;
    public final String currentGuessState;


    public GameState(int points, String word, int lives) {
        this.points = points;
        this.lives = lives;
        this.currentGuessState = word;
    }
    @Override
    public String toString() {
        return "{\"points\":" + String.valueOf(points) + ", \"word\":\"" + currentGuessState + ", \"lives\":" + String.valueOf(lives) + '}';
    }
}

