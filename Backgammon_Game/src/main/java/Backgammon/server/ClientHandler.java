package Backgammon.server;
import Backgammon.common.GameMessage;
import java.io.*;
import java.net.Socket;

//her client  için ayrı çalışan thread yapısıdır
//Oyuncudan gelen mesajları dinler ve ilgili GameRoom metoduna yönlendirir
public class ClientHandler implements Runnable {

    private final Socket           socket;  //oyuncunun server ile bağlantısı
    private final BackgammonServer server;  //handlerin bağlı olduğu ana server nesnesi oyuncu hazırsa ya da bağlantısı koptuysa 
    private final int              playerID;
    private ObjectInputStream  in;  //clienttan gelen mesajları okur
    private ObjectOutputStream out; //clienta mesaj gönderirr
    private String             username;
    private GameRoom           gameRoom;
    private volatile boolean running;  //oyuncu bağlantısı aktif mi
    private volatile boolean joined;  //volatile değişkenlerin farklı threadler arasında güncel okunmasını sağlar
    private int wins;

    //client bağlantısı server referansı ve oyuncu idsi ile handler oluştur
    public ClientHandler(Socket socket, BackgammonServer server, int playerID) {
        this.socket   = socket;
        this.server   = server;
        this.playerID = playerID;
        this.username = "Oyuncu" + playerID;
    }

    
    //thread çalıştığında socket streamlerini oluşturur ve client mesajlarını dinlemeye başlar
    @Override
    public void run() {
        try {
            socket.setKeepAlive(true); 
            socket.setSoTimeout(0); //zaman aşımı olmaz server oyuncudan mesaj gelene kadar bekler
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in  = new ObjectInputStream(socket.getInputStream());
            running = true;
            ServerLogger.logNetwork("Oyuncu " + playerID + " baglandi: " + socket.getInetAddress().getHostAddress());
            listen();
        } catch (IOException e) {
            ServerLogger.logError("Oyuncu " + playerID + " akis hatasi: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    //clienttan gelen gameMessage nesnelerini okur
    private void listen() {
        while (running) {
            try {
                GameMessage msg = (GameMessage) in.readObject();
                if (msg != null) handle(msg);
            } catch (IOException e) {
                if (running) ServerLogger.logNetwork("Oyuncu " + playerID + " baglantisi kesildi.");
                break;
            } catch (ClassNotFoundException e) {
                ServerLogger.logError("Bilinmeyen mesaj: " + e.getMessage());
            }
        }
    }

    //gelen mesaja göre işlemi çağırır
    private void handle(GameMessage msg) {
        ServerLogger.log("Mesaj [Oyuncu " + playerID + "]: " + msg.getType());
        switch (msg.getType()) {
            case PLAYER_JOIN:     handlePlayerJoin(msg);                              break;
            case ROLL_DICE:       if (gameRoom != null) gameRoom.rollDice(playerID);  break;
            case MOVE_PIECE:      if (gameRoom != null) gameRoom.processMove(msg);    break;
            case REMATCH_REQUEST: if (gameRoom != null) gameRoom.handleRematchRequest(playerID); break;
            default: ServerLogger.logWarning("Islenemeyen mesaj: " + msg.getType());
        }
    }

    
    //oyuncunun usernameini alır ve servera hazır olduğunu söyler
    private void handlePlayerJoin(GameMessage msg) {
        if (msg.getData() instanceof String) {
            String name = ((String) msg.getData()).trim();
            if (!name.isEmpty()) username = name;
        }
        if (!joined) {
            joined = true;
            ServerLogger.log("Oyuncu " + playerID + " katildi: " + username);
            try { Thread.sleep(200); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            server.onClientReady(this);
        }
    }

    //clienta mesaj gönderir
    public synchronized void sendMessage(GameMessage msg) {
        try {
            if (out != null && !socket.isClosed()) {
                out.writeObject(msg); out.flush(); out.reset();
            }
        } catch (IOException e) {
            ServerLogger.logError("Oyuncu " + playerID + "'e gonderilemedi: " + e.getMessage());
        }
    }

    //client bağlantısını kapatır oyun odasına haber verir ve server listesinden çıkarır
    public void disconnect() {
        if (!running) return;
        running = false;
        if (gameRoom != null && gameRoom.isActive()) gameRoom.handleDisconnect(playerID);
        server.removeClient(this);
        try {
            if (in  != null) in.close();
            if (out != null) out.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            ServerLogger.logError("Baglanti kapatilamadi: " + e.getMessage());
        }
        ServerLogger.logNetwork("Oyuncu " + playerID + " temizlendi.");
    }

    public void     incrementWins()         { wins++; }
    public int      getWins()               { return wins; }
    public int      getPlayerID()           { return playerID; }
    public String   getUsername()           { return username; }
    public GameRoom getGameRoom()           { return gameRoom; }
    public void     setGameRoom(GameRoom r) { this.gameRoom = r; }
    public boolean  isRunning()             { return running; }
    public boolean  isJoined()              { return joined; }
}