package client.view;

public class OutputManager {

    synchronized void println(String output) {
        System.out.println(output);
    }

    synchronized void print(String output) {
        System.out.print(output);
    }
}
