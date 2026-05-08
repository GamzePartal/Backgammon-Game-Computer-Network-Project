
package Backgammon.client;
import Backgammon.common.GameMessage;
import Backgammon.common.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class BackgammonClient {
 
    // Bağlanılacak sunucunun IP adresi 
    private String serverIP;
 
    // Bağlanılacak sunucunun port numarası
    private int serverPort;
 
    // TCP soket bağlantısı
    private Socket socket;
 
    // Sunucuya nesne göndermek için çıkış akışı
    private ObjectOutputStream outputStream;
 
    // Sunucudan nesne almak için giriş akışı
    private ObjectInputStream inputStream;
 
    // Mesaj dinleme thread'inin çalışıp çalışmadığını gösteren bayrak
    private boolean listening;
 
    // Gelen mesajları işleyen ve ekran geçişlerini yöneten ekran yöneticisi
    private ScreenManager screenManager;
 
    // Bu istemcinin sunucu tarafından atanan oyuncu ID'si
    private int playerID;
 
    // Bu istemcinin kullanıcı adı (giriş ekranından alınır)
    private String username;
 
    
    public BackgammonClient(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.listening = false;
    }
 
    
    public boolean connect(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
 
        try {
            // Sunucuya TCP bağlantısı kur
            socket = new Socket(serverIP, serverPort);
 
            // Çıkış akışını giriş akışından ÖNCE oluştur (deadlock önlemi)
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
 
            // Mesaj dinleme thread'ini başlat
            startListening();
 
            System.out.println("[İSTEMCİ] Sunucuya bağlandı: " + ip + ":" + port);
            return true;
 
        } catch (IOException e) {
            System.err.println("[İSTEMCİ] Bağlantı hatası: " + e.getMessage());
            return false;
        }
    }
 
   
    private void startListening() {
        listening = true;
 
        Thread listenerThread = new Thread(() -> {
            while (listening) {
                try {
                    // Blokeli okuma: mesaj gelene kadar bekler
                    GameMessage message = (GameMessage) inputStream.readObject();
                    if (message != null) {
                        handleServerMessage(message);
                    }
                } catch (IOException e) {
                    if (listening) {
                        System.err.println("[İSTEMCİ] Sunucu bağlantısı kesildi.");
                        // Ekran yöneticisine bağlantı kopmasını bildir
                        screenManager.onDisconnected();
                    }
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("[İSTEMCİ] Bilinmeyen mesaj alındı: " + e.getMessage());
                }
            }
        }, "ServerListener");
 
        // Daemon thread: ana uygulama kapanınca bu da kapansın
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
 
  
   
    public synchronized void sendMessage(GameMessage message) {
        try {
            if (outputStream != null && isConnected()) {
                outputStream.writeObject(message);
                outputStream.flush();
                // Nesne önbelleğini temizle (güncel veri gönderilmesini sağlar)
                outputStream.reset();
            }
        } catch (IOException e) {
            System.err.println("[İSTEMCİ] Mesaj gönderilemedi: " + e.getMessage());
        }
    }
 
   
    public void sendRollDice() {
        System.out.println("[DEBUG] sendRollDice - playerID: " + playerID);
    GameMessage msg = new GameMessage(MessageType.ROLL_DICE, playerID);
    sendMessage(msg);
    }

    public void sendMove(int from, int to, int dieVal) {
        System.out.println("[DEBUG] sendMove - playerID: " + playerID + " from:" + from + " to:" + to);
    int[] moveData = {from, to, dieVal};
    GameMessage msg = new GameMessage(MessageType.MOVE_PIECE, playerID, moveData);
    sendMessage(msg);
    }
 
   
  
 
   
    public void sendPlayerJoin(String username) {
        this.username = username;
        GameMessage msg = new GameMessage(MessageType.PLAYER_JOIN, playerID, username);
        sendMessage(msg);
    }
 
   
  
    private void handleServerMessage(GameMessage message) {
        // Tüm mesaj işleme ScreenManager üzerinden yapılır
        // ScreenManager içinde SwingUtilities.invokeLater ile EDT'ye geçilir
        screenManager.onMessageReceived(message);
    }
 
   
    public void disconnect() {
        listening = false;
 
        try {
            if (inputStream != null)  inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[İSTEMCİ] Bağlantı kapatılırken hata: " + e.getMessage());
        }
 
        System.out.println("[İSTEMCİ] Sunucu bağlantısı kapatıldı.");
    }
 
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
 
   
    public int getPlayerID() {
        return playerID;
    }
 
   
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
 
    
    public String getUsername() {
        return username;
    }
 
   
    public String getServerIP() {
        return serverIP;
    }
 
   
    public int getServerPort() {
        return serverPort;
    }
}
