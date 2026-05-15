package Backgammon.common;
import java.io.Serializable;

//client ve server arasında gönderilen mesaj nesnesi
public class GameMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private MessageType type;
    private int senderID;
    private Object data;

    //veri içermeyen mesaj
    public GameMessage(MessageType type, int senderID) {
        this(type, senderID, null);
    }

    public GameMessage(MessageType type, int senderID, Object data) {
        this.type     = type;
        this.senderID = senderID;
        this.data     = data;
    }

    public MessageType getType()               { return type; }
    public void        setType(MessageType t)  { this.type = t; }
    public int         getSenderID()           { return senderID; }
    public void        setSenderID(int id)     { this.senderID = id; }
    public Object      getData()               { return data; }
    public void        setData(Object d)       { this.data = d; }
}