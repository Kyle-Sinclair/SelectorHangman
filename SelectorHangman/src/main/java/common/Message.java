package common;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Message implements Serializable {

    public final MessageType type;
    public final Object payload;


    public Message(MessageType type, String payload){
        this.type = type;
        this.payload = payload;
    }

    public static String serialize(Message message) {
        return message.type.toString() + "##" + message.payload;
    }

    /*
     * Decode message from string.
     */
    public static Message deserialize(byte[] messageBytes) {
        String received = new String(messageBytes, StandardCharsets.UTF_8);
        String[] parts = received.split("##");
        MessageType type = MessageType.valueOf(parts[0].toUpperCase());
        String body = parts.length > 1 ? parts[1] : "";
        return new Message(type, body);
    }

    public Object getPayload(){ return payload; }

    @Override
    public String toString(){
        return '{' + "\"type\":\""  + type + "\", \"payload\":\"" + payload + "\"}";
    }
}
