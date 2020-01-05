package client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

class CmdLine {
    private ArrayList<String> arguments = new ArrayList<>();
    private Command cmd;

    /**
     * Constructor for CmdLine is responsible for processing entered text
     * @param rawText User input.
     */
    CmdLine(String rawText) {
        parseRawText(rawText);
    }

    /**
     * @param rawText User input.
     */

    private void parseRawText(String rawText) {
        StringTokenizer tokenizer = new StringTokenizer(rawText);

        if (tokenizer.countTokens() == 0) {
            this.cmd = Command.NO_COMMAND;
        }

        String cmd = tokenizer.nextToken().toUpperCase();

        switch (cmd) {
            case "CONNECT":
                this.cmd = Command.CONNECT;
                break;
            case "QUIT":
                this.cmd = Command.QUIT;
                break;
            case "START":
                this.cmd = Command.START;
                break;
            default:
                this.cmd = Command.GUESS;
                 String guess = tokenizer.nextToken().toUpperCase();
                arguments.add(guess);
        }
    }

    /**
     * @return The stored command type.
     */
    Command getCmd() {
        return cmd;
    }

    /**
     * @return one of the arguments stored in the line. Different
     * arguments have different index codes
     * A guess string = 0
     */

    public String getArgument(int index){
        return arguments.get(index);
    }

}
