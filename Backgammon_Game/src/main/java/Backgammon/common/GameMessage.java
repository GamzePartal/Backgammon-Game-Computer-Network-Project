
package Backgammon.common;
import java.io.Serializable;

public class GameMessage implements Serializable  {
    
    // Java serileştirme versiyonu sınıf değiştiğinde uyumsuzluk hatasını önler
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private int senderID; // mesajı gönderen oyuncunun sunucu tarafından atanan kimlik numarası
    private Object data;  // Mesajın taşıdığı asıl veri (hamle bilgisi, zar değeri, sohbet metni vb.)
 
    //veri içermeyen mesajlar için uygun
    public GameMessage(MessageType type, int senderID) {
        this.type = type;
        this.senderID = senderID;
    }
 
  
   //Tip, gönderen ve veri alanıyla tam mesaj oluşturmak için kullanılan yapıcı.
    public GameMessage(MessageType type, int senderID, Object data) {
        this.type = type;
        this.senderID = senderID;
        this.data = data;
    }


    public MessageType getType() {
        return type;
    }
 

    public void setType(MessageType type) {
        this.type = type;
    }
 
    public int getSenderID() {
        return senderID;
    }
 
 
    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }
 
    
    public Object getData() {
        return data;
    }
 
   

    public void setData(Object data) {
        this.data = data;
    }
 

    
}
