package Backgammon.server;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// Client bağlantılarını kabul eder, oyuncuları eşleştirir ve oyun odası oluşturur
public class BackgammonServer {

    private static final int DEFAULT_PORT = 5000;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients   = new ArrayList<>(); //servera bağlanıp hazır olan clientları tutar
    private final List<GameRoom>  gameRooms = new ArrayList<>();  //oluşturulan oyun odalarını tutar
    private final AtomicInteger playerIDCounter  = new AtomicInteger(1);
    private final AtomicInteger roomIDCounter    = new AtomicInteger(1);
    private boolean running;// server çalışıyor mu

    
    // Sunucuyu belirtilen portta başlatır
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            ServerLogger.logStartup(port);
            acceptConnections();
        } catch (IOException e) {
            ServerLogger.logError("Sunucu baslatilamadi: " + e.getMessage());
        } finally {
            shutdown(); //server kapanırken tüm bağlantılar temizlenir hata alındıysa
        }
    }

    // Gelen bağlantıları kabul eder her client için ayrı thread açar
    private void acceptConnections() {
        ServerLogger.log("Baglanti bekleniyor...");
        while (running) {
            try {
                Socket s         = serverSocket.accept();  //accept fonk client bağlanana kadar beklerbi client bağlanınca onun için bi socket döndrür
                int    newID     = playerIDCounter.getAndIncrement();
                ClientHandler ch = new ClientHandler(s, this, newID);
                Thread t         = new Thread(ch, "Oyuncu-" + newID); //her oyuncu için ayrı bi tread oluştur çünkü server birdenfazla oyuncuyu dinler
                t.setDaemon(true); //program kapanırken uygulmayı açık tutmaz
                t.start();  //burdan sonra clienthandler içindeki run methodu çalışır
                ServerLogger.logNetwork("Yeni baglanti, ID: " + newID);
            } catch (IOException e) {
                if (running) ServerLogger.logError("Baglanti hatasi: " + e.getMessage());
            }
        }
    }

    // Oyuncu hazır olunca listeye ekler ve eşleşme dener
    // synchronized çünkü aynı anda birden fazla oyuncu hazır olabilir karışıklığı önler
    public synchronized void onClientReady(ClientHandler handler) {
        if (!clients.contains(handler)) {
            clients.add(handler);
            ServerLogger.log("Oyuncu " + handler.getPlayerID()+" listeye eklendi");
        }
        tryMatch(); //oyuncu listye eklenince eşleşme beklenir
    }

    // Bekleyen oyuncuları ikili eşleştirir ve GameRoom başlatır
    private synchronized void tryMatch() {
        List<ClientHandler> waiting = new ArrayList<>(); //bekleyenler için geçici liste oluştur
        for (ClientHandler h : clients)
            if (h.getGameRoom() == null && h.isRunning() && h.isJoined())
                waiting.add(h);

        while (waiting.size() >= 2) {
            ClientHandler p1 = waiting.remove(0);
            ClientHandler p2 = waiting.remove(0);
            String roomId    = "Oda-" + roomIDCounter.getAndIncrement();
            GameRoom room    = new GameRoom(roomId, p1, p2);
            gameRooms.add(room);
            p1.setGameRoom(room);
            p2.setGameRoom(room);
            ServerLogger.log("Oda: " + roomId + " (" + p1.getUsername() + " vs " + p2.getUsername() + ")");
            Thread t = new Thread(room::initGame, "Oyun-" + roomId);  //oyunu başlatır
            t.setDaemon(true);
            t.start();
        }
    }

    // Bağlantısı kopan clientı listeden siler
    public synchronized void removeClient(ClientHandler handler) {
        clients.remove(handler);
        ServerLogger.logNetwork("Oyuncu " + handler.getPlayerID() + " kaldirildi. Kalan: " + clients.size());
    }

   

    // Sunucuyu kapatır
    public void shutdown() {
        running = false;
        ServerLogger.logShutdown();
        for (ClientHandler h : new ArrayList<>(clients)) h.disconnect();
        clients.clear();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            ServerLogger.logError("Soket kapatilamadi: " + e.getMessage());
        }
    }

    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0)
            try { port = Integer.parseInt(args[0]); }
            catch (NumberFormatException e) { ServerLogger.logWarning("Gecersiz port, varsayilan: " + DEFAULT_PORT); }

        BackgammonServer server = new BackgammonServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "ShutdownHook"));
        server.start(port);
    }
}