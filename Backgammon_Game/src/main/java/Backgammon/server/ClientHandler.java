package Backgammon.server;

import Backgammon.common.GameMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable{

    private final Socket socket; // İstemcinin TCP soket bağlantısı
    private final BackgammonServer server;
    private ObjectInputStream inputStream; // Java serileştirilmiş nesneleri okumak için giriş akışı
    private ObjectOutputStream outputStream;   // Java serileştirilmiş nesneleri göndermek için çıkış akışı
    private final int playerID; // server tarafından oyuncuya atanan id
    private String username;
    private GameRoom gameRoom; // Bu clientın bağlı olduğu oyun odası eşleşme sonrası atanır
    private boolean running;  

 
  
    public ClientHandler(Socket socket, BackgammonServer server, int playerID) {
        this.socket = socket;
        this.server = server;
        this.playerID = playerID;
        this.running = false;
        this.username = "Oyuncu" + playerID; // Varsayılan kullanıcı adı
    }

    
    //bağlantı kopna kadar ya da hata oluşana kadar devam eder
      @Override
    public void run() {
        try {
            // Çıkış akışını giriş akışından önce oluştur (deadlock önlemi)
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());

            running = true;
            ServerLogger.logNetwork("Oyuncu " + playerID + " bağlandı: " + socket.getInetAddress().getHostAddress());

            server.onClientReady(this); // Sunucuya yeni istemci hazır olduğunu bildir
            
            startListening(); // Mesaj dinleme döngüsü bağlantı kopana kadar çalışır

        } catch (IOException e) {
            ServerLogger.logError("Oyuncu " + playerID + " akış hatası: " + e.getMessage());
        } finally {
            disconnect(); // Her durumda bağlantıyı temizle
        }
    }

    
    // clienttan gelen mesjları işler , ObjectInputStream üzerinden GameMessage nesneleri okunur
    private void startListening() {
        while (running) {
            try {
                //yeni mesaj gelene kadar bekler
                GameMessage message = (GameMessage) inputStream.readObject();
                if (message != null) {
                    handleMessage(message);
                }
            } catch (IOException e) {
                // bağlantı koptu veya soket kapandı
                if (running) {
                    ServerLogger.logNetwork("Oyuncu " + playerID + " bağlantısı kesildi.");
                }
                break;
            } catch (ClassNotFoundException e) {
                ServerLogger.logError("Bilinmeyen mesaj tipi alındı: " + e.getMessage());
            }
        }
    }

   
    
    // gelen gameMessage nnesnesini gerekli methoda yönlendirir
    private void handleMessage(GameMessage message) {
        ServerLogger.log("Mesaj alındı [Oyuncu " + playerID + "]: " + message.getType());

        switch (message.getType()) {

            case PLAYER_JOIN:       
                handlePlayerJoin(message);  // Oyuncu bağlandığında kullanıcı adını kaydet
                break;

            case ROLL_DICE: 
                if (gameRoom != null) { 
                    gameRoom.rollDice(playerID); // Zar atma talebini gameRooma ilet
                }
                break;

            case MOVE_PIECE:              
                if (gameRoom != null) {
                    gameRoom.processMove(message); // hamle yapma talebi
                }
                break;

       
            case REMATCH_REQUEST:              
                ServerLogger.log("Oyuncu " + playerID + " tekrar oynama istiyor.");  // Tekrar oynama talebi 
                break;

            default:
                ServerLogger.logWarning("İşlenemeyen mesaj tipi: " + message.getType());
                break;
        }
    }

    
    // oyuncunun bağlandı mesajı
    private void handlePlayerJoin(GameMessage message) {
        if (message.getData() instanceof String) {
            this.username = (String) message.getData();
        }
        ServerLogger.log("Oyuncu " + playerID + " katıldı: " + username);
    }

    
    
    //clienta gameMessage nesnesi gönderir ve bu işlem thread safe olması için synchronized edilir
    public synchronized void sendMessage(GameMessage message) {
        try {
            if (outputStream != null && !socket.isClosed()) {
                outputStream.writeObject(message);
                outputStream.flush(); // mesaj hemen iletilir  
                outputStream.reset(); // önbelleği temizle
            }
        } catch (IOException e) {
            ServerLogger.logError("Oyuncu " + playerID + "'e mesaj gönderilemedi: "+ e.getMessage());
        }
    }

    
    //client bağlantısını kapatır ve servera söyler. soket giriş ve çıkış akışlarını temizler
    public void disconnect() {
        if (!running) {
            return; 
        }
        running = false;

        // oyun odası bilgilendirilir rakibe mesaj gönder
        if (gameRoom != null && gameRoom.isActive()) {
            gameRoom.handleDisconnect(playerID);
        }

        // serverdan bu handlerı kaldır
        server.removeClient(this);

        // Soket ve akışları kapat
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            ServerLogger.logError("Bağlantı kapatılamadı: " + e.getMessage());
        }

        ServerLogger.logNetwork("Oyuncu " + playerID + " bağlantısı temizlendi.");
    }

    
    public int getPlayerID() {
        return playerID;
    }

    
    public String getUsername() {
        return username;
    }

    
    public GameRoom getGameRoom() {
        return gameRoom;
    }

    
    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    
    public boolean isRunning() {
        return running;
    }

}
