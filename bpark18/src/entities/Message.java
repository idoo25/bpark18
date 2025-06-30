package entities;

import java.io.Serializable;

/**
 * Message entity for client-server communication.
 * Single Responsibility: Encapsulates message data between client and server.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private Serializable content;
    
    /**
     * Constructor
     */
    public Message(MessageType type, Serializable content) {
        this.type = type;
        this.content = content;
    }
    
    // Getters and setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public Serializable getContent() {
        return content;
    }
    
    public void setContent(Serializable content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "Message{type=" + type + ", content=" + content + "}";
    }
}