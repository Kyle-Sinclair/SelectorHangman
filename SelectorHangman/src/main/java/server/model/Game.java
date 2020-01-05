package server.model;

import common.Definitions;
import common.GameState;
import common.Message;
import common.MessageType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static common.MessageType.START_RESPONSE;

public class Game {


    private static final ArrayList<String> DICTIONARY = new ArrayList();
    private static final Random RNG = new Random();
    private String currentState, wordToGuess;
    private int score, lives;

    private final boolean wrong_guesses[] = new boolean[26];
    private final boolean repeat_guesses[] = new boolean[26];


    public Game(){
        this.score = 0;
        this.lives = 0;
    }



    public static void initializeDictionary(String dictName) throws FileNotFoundException, IOException {

        File file = new File(dictName);
        if (!file.exists() || !file.canRead())
            throw new FileNotFoundException(dictName);

        FileReader fileReader = new FileReader(file);
        BufferedReader in = new BufferedReader(fileReader);

        String line;
        while ((line = in.readLine()) != null) { DICTIONARY.add(line.toUpperCase()); }

        fileReader.close();
        DICTIONARY.trimToSize();
        System.out.println("Dictionary of words loaded. Number of words: " + DICTIONARY.size());
    }



    private static String toFullForm(String item) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < item.length(); i++) {
            sb.append(item.charAt(i));
            if (i + 1 != item.length())
                sb.append(' ');
        }
        return sb.toString();
    }




    private GameState getSnapshot(){
        return new GameState(score, toFullForm(currentState), this.lives);
    }

    public Message makeAGuess(char guess){
        //System.out.println("Guessing a single letter");

        if(!isALegalGuess("" + guess)) { return buildServerInterfaceResponse(MessageType.ILLEGAL_RESPONSE);}
        if(repeat_guesses[guess - 'A']) { return buildServerInterfaceResponse(MessageType.REPEAT_RESPONSE); }

        if (wordToGuess.contains("" + guess)) {
            repeat_guesses[guess - 'A'] = true;
            updateCurrentState(guess);

            if(isGameWon()){ score++;
                            newRound();
                            return buildServerInterfaceResponse(MessageType.VICTORY_RESPONSE); }
            else { return buildServerInterfaceResponse(MessageType.CORRECT_RESPONSE); }
        }

        else {
            this.lives--;
            repeat_guesses[guess - 'A'] = true;
            if(lives <= 0) {score--;
                            newRound();
                            return buildServerInterfaceResponse(MessageType.LOSS_RESPONSE); }

            return buildServerInterfaceResponse(MessageType.INCORRECT_RESPONSE); }

    }

    public Message makeAGuess(String guess){

        if(wordToGuess.equals(guess)) {
            score++;
            newRound();
            return buildServerInterfaceResponse(MessageType.VICTORY_RESPONSE);
        }
        else {
            --lives;
            if (lives <= 0) {
                score--;
                newRound();
                return buildServerInterfaceResponse(MessageType.LOSS_RESPONSE); }

            return buildServerInterfaceResponse (MessageType.INCORRECT_RESPONSE);
        }

    }

        private void guessSingleLetter(){

        }

        private void guessWholeWord(){

        }


        /*
        * Response Functions
         */
        private Message buildServerInterfaceResponse(MessageType messageType){
            GameState state = getSnapshot();
            String currentState = state.toString();
            System.out.println(currentState);
            String textResult = "";

            switch(messageType){

                case START_RESPONSE:
                    textResult = "New Game Started.";
                    break;
                case ILLEGAL_RESPONSE:
                    textResult = "Illegal guess.";
                    break;
                case CORRECT_RESPONSE:
                    textResult = "Hit!";
                    break;
                case INCORRECT_RESPONSE:
                    textResult = "Incorrect!";
                    break;
                case VICTORY_RESPONSE:
                    textResult = "You won a round!";
                    break;
                case REPEAT_RESPONSE:
                    textResult = "You've guessed this before ";
                    break;
                case LOSS_RESPONSE:
                    textResult = "Too bad. You lose.";
                    break;
                default:
                    throw new IllegalStateException("Unexpected value in the buildServerInterfaceResponse method" );
            }
            return new Message(messageType,textResult + " Game Status " + currentState);
        }


    /*
    * Sanity Checking Functions
     */

    private boolean isSingleLetter(String guess){
        return guess.length() == 1;
    }
    public boolean isALegalGuess(String guess) {
        return (guess.length() == 1 || guess.length() == this.wordToGuess.length()) && Definitions.LEGAL_CHARS.contains("" + guess.toUpperCase());
    }

    private boolean isInValidState() {
        return (wordToGuess != null && wordToGuess.compareTo(currentState) != 0 && lives > 0);
    }

    private boolean isRepeatGuess(char guess){
        return wrong_guesses[guess-'A'];
    }
    private boolean isIncorrectGuess(){
        return true;
    }

    private boolean isCorrectGuess(){
        return true;
    }
    private boolean isGameWon(){
        return !currentState.contains("_");
    }

    private void chooseWord(){
        wordToGuess = DICTIONARY.get(RNG.nextInt(DICTIONARY.size()));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++)
            sb.append('_');
        currentState = sb.toString();
    }

    public Message startNewGame(){
        chooseWord();
        newRound();
        return buildServerInterfaceResponse(MessageType.START_RESPONSE);
    }



    private void updateCurrentState(Character guess) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.charAt(i) == guess)
                sb.append(guess);
            else
                sb.append(currentState.charAt(i));
        }
        currentState = sb.toString();
    }

    public void newRound() {
        chooseWord();
        lives = wordToGuess.length();
        for (int i = 0; i < repeat_guesses.length; i++) { repeat_guesses[i] = false; }
    }
}



