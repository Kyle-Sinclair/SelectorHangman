package common;


public class MessageException extends RuntimeException {
    public MessageException(String msg) {
        super(msg);
    }

    public MessageException(Throwable cause) {
        super(cause);
    }
}
