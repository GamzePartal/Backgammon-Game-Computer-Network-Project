package Backgammon.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


//oyunun ana sunucu sınıfırdır. aws üzerinde konsoldan çalışır, client bağlantılarını kabul eder eşleştirir vs
public class BackgammonServer {

    private static final int DEFAULT_PORT = 5000;    // Sunucunun dinleyeceği varsayılan port numarası
    private ServerSocket serverSocket; // gelen TCP bağlantılarını kabul eder
    private final List<ClientHandler> connectedClients;   // Bağlı istemcilerin handler listesi (senkronize erişim gerektirir)
    private final List<GameRoom> gameRooms; // Aktif oyun odalarının listes
    private final AtomicInteger playerIDCounter; // Her yeni istemciye benzersiz ID atamak için artımlı sayaç
    private final AtomicInteger roomIDCounter; // Oyun odalarına benzersiz isim üretmek için artımlı sayaç
    private boolean running; // server çaılışıyor mu

    
    // start çağırana kadar port açılmaz
    public BackgammonServer() {
        this.connectedClients = new ArrayList<>();
        this.gameRooms = new ArrayList<>();
        this.playerIDCounter = new AtomicInteger(1);
        this.roomIDCounter = new AtomicInteger(1);
        this.running = false;
    }

   
    // server belirtilen portta başlatılır client bağlantısı beklemeye alır
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            ServerLogger.logStartup(port);

            acceptConnections();

        } catch (IOException e) {
            ServerLogger.logError("Sunucu başlatılamadı: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

  
    //gelen istemci bağlantılarını sürekli kabul eder her yeni bağlantı için clienthandler oluşturur
    private void acceptConnections() {
       
        ServerLogger.log("Bağlantı bekleniyor...");
        while (running) {
            try {
                
                Socket clientSocket = serverSocket.accept(); // Yeni bağlantı gelene kadar bekle

                int newPlayerID = playerIDCounter.getAndIncrement();  // yeni istemciye id ata

                // Handler oluştur ve thread başlat
                ClientHandler handler = new ClientHandler(clientSocket, this, newPlayerID);
                Thread clientThread = new Thread((Runnable) handler, "Oyuncu-" + newPlayerID);
                clientThread.setDaemon(true); // sunucu kapanınca thread de kapansın
                clientThread.start();

                ServerLogger.logNetwork("Yeni bağlantı kabul edildi, Oyuncu ID: " + newPlayerID);

            } catch (IOException e) {
                if (running) {
                    ServerLogger.logError("Bağlantı kabul hatası: " + e.getMessage());
                }
                // running = false ise sunucu kapatılıyor, döngüden çık
            }
        }
    }


    // server kapatılır soket ve aktif bağlantılar silinir
    public void shutdown() {
        running = false;
        ServerLogger.logShutdown();

        // Tüm istemci bağlantılarını kapat
        synchronized (connectedClients) {
            for (ClientHandler handler : connectedClients) {
                handler.disconnect();
            }
            connectedClients.clear();
        }

        // server soketini kapat
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            ServerLogger.logError("Sunucu soketi kapatılamadı: " + e.getMessage());
        }
    }

    
    //client hazır olduğunda clienthandler tarafından çağırılır clienti listye ekleyip eşleşme kontrolu yapar
    // iki oyuncu olursa oyun başlar
    public synchronized void onClientReady(ClientHandler handler) {
        connectedClients.add(handler);
        ServerLogger.log("Oyuncu " + handler.getPlayerID() + " listeye eklendi. "+ "Toplam bekleyen: " + getWaitingCount());

        tryMatchPlayers();  // bekleyen oyuncuları eşleştir
    }

   
    //bekleyen oyuncuları eşleştirir ve oyunu başlatır
    private void tryMatchPlayers() {
        
        List<ClientHandler> waitingPlayers = new ArrayList<>(); // Oyun odası olmayan oyuncuları filtrele
        synchronized (connectedClients) {
            for (ClientHandler h : connectedClients) {
                if (h.getGameRoom() == null && h.isRunning()) {
                    waitingPlayers.add(h);
                }
            }
        }

        // En az iki bekleyen oyuncu varsa eşleştir
        while (waitingPlayers.size() >= 2) {
            ClientHandler p1 = waitingPlayers.remove(0);
            ClientHandler p2 = waitingPlayers.remove(0);

            String roomId = "Oda-" + roomIDCounter.getAndIncrement();

            GameRoom room = new GameRoom(roomId, p1, p2); // oyun odası oluştur
            gameRooms.add(room);
            p1.setGameRoom(room); 
            p2.setGameRoom(room);

            ServerLogger.log("Oyun odası oluşturuldu: " + roomId+ " (" + p1.getUsername() + " vs " + p2.getUsername() + ")");

            // Oyunu ayrı thread'de başlat (initGame bloklamasın)
            final GameRoom finalRoom = room;
            Thread gameThread = new Thread(() -> finalRoom.initGame(), "Oyun-" + roomId);
            gameThread.setDaemon(true);
            gameThread.start();
        }
    }

   
    //bağlantısı kopan clientı listeden sil clientHandler.disconnect çağırır
    public synchronized void removeClient(ClientHandler handler) {
        synchronized (connectedClients) {
            connectedClients.remove(handler);
        }
        ServerLogger.logNetwork("Oyuncu " + handler.getPlayerID() + " listeden kaldırıldı. Kalan: " + connectedClients.size());
    }

    
    //bekleyen oyuncu sayısını döner
    private int getWaitingCount() {
        int count = 0;
        synchronized (connectedClients) {
            for (ClientHandler h : connectedClients) {
                if (h.getGameRoom() == null) {
                    count++;
                }
            }
        }
        return count;
    }

    
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Komut satırından port numarası alındıysa kullan
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                ServerLogger.logWarning("Geçersiz port: " + args[0] + " - Varsayılan port kullanılıyor: " + DEFAULT_PORT);
            }
        }

        BackgammonServer server = new BackgammonServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "ShutdownHook"));

 
        server.start(port);
    }
}