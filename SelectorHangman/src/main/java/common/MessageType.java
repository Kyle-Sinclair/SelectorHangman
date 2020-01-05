package common;

public enum MessageType {
    //client specific message types
    DISCONNECT,
    GUESS,
    START,

    //Server specific Message Types always end with _RESPONSE
    ILLEGAL_RESPONSE,
    VICTORY_RESPONSE,
    CORRECT_RESPONSE,
    INCORRECT_RESPONSE,
    LOSS_RESPONSE,
    REPEAT_RESPONSE,
    GUESS_RESPONSE,
    START_RESPONSE,
    DISCONNECT_RESPONSE

}
