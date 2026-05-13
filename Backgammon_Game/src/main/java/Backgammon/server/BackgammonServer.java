package Backgammon.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// Oyunun ana sunucu sınıfıdır.
// AWS üzerinde konsoldan çalışır, client bağlantılarını kabul eder ve oyuncuları eşleştirir.
public class BackgammonServer {

    private static final int DEFAULT_PORT = 5000;

    private ServerSocket serverSocket;
    private final List<ClientHandler> connectedClients;
    private final List<GameRoom> gameRooms;
    private final AtomicInteger playerIDCounter;
    private final AtomicInteger roomIDCounter;
    private boolean running;

    public BackgammonServer() {
        this.connectedClients = new ArrayList<>();
        this.gameRooms = new ArrayList<>();
        this.playerIDCounter = new AtomicInteger(1);
        this.roomIDCounter = new AtomicInteger(1);
        this.running = false;
    }

    // Server belirtilen portta başlatılır.
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            ServerLogger.logStartup(port);

            acceptConnections();

        } catch (IOException e) {
            ServerLogger.logError("Sunucu baslatilamadi: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    // Gelen client bağlantılarını kabul eder.
    private void acceptConnections() {
        ServerLogger.log("Baglanti bekleniyor...");

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();

                int newPlayerID = playerIDCounter.getAndIncrement();

                ClientHandler handler = new ClientHandler(clientSocket, this, newPlayerID);

                Thread clientThread = new Thread(handler, "Oyuncu-" + newPlayerID);
                clientThread.setDaemon(true);
                clientThread.start();

                ServerLogger.logNetwork("Yeni baglanti kabul edildi, Oyuncu ID: " + newPlayerID);

            } catch (IOException e) {
                if (running) {
                    ServerLogger.logError("Baglanti kabul hatasi: " + e.getMessage());
                }
            }
        }
    }

    // Client PLAYER_JOIN mesajını gönderip ismini belirledikten sonra çağrılır.
    // Yani oyuncu socket açınca değil, gerçekten oyuna katılınca hazır sayılır.
    public synchronized void onClientReady(ClientHandler handler) {
        synchronized (connectedClients) {
            if (!connectedClients.contains(handler)) {
                connectedClients.add(handler);

                ServerLogger.log("Oyuncu " + handler.getPlayerID()
                        + " listeye eklendi. Toplam bekleyen: " + getWaitingCount());
            }
        }

        tryMatchPlayers();
    }

    // Bekleyen ve ismini göndermiş oyuncuları ikişerli eşleştirir.
    private synchronized void tryMatchPlayers() {
        List<ClientHandler> waitingPlayers = new ArrayList<>();

        synchronized (connectedClients) {
            for (ClientHandler h : connectedClients) {
                if (h.getGameRoom() == null && h.isRunning() && h.isJoined()) {
                    waitingPlayers.add(h);
                }
            }
        }

        while (waitingPlayers.size() >= 2) {
            ClientHandler p1 = waitingPlayers.remove(0);
            ClientHandler p2 = waitingPlayers.remove(0);

            String roomId = "Oda-" + roomIDCounter.getAndIncrement();

            GameRoom room = new GameRoom(roomId, p1, p2);
            gameRooms.add(room);

            p1.setGameRoom(room);
            p2.setGameRoom(room);

            ServerLogger.log("Oyun odasi olusturuldu: " + roomId
                    + " (" + p1.getUsername() + " vs " + p2.getUsername() + ")");

            Thread gameThread = new Thread(() -> room.initGame(), "Oyun-" + roomId);
            gameThread.setDaemon(true);
            gameThread.start();
        }
    }

    // Bağlantısı kopan client'ı listeden siler.
    public synchronized void removeClient(ClientHandler handler) {
        synchronized (connectedClients) {
            connectedClients.remove(handler);
        }

        ServerLogger.logNetwork("Oyuncu " + handler.getPlayerID()
                + " listeden kaldirildi. Kalan: " + connectedClients.size());
    }

    // Odaya girmemiş ve ismini göndermiş bekleyen oyuncu sayısını döndürür.
    private int getWaitingCount() {
        int count = 0;

        synchronized (connectedClients) {
            for (ClientHandler h : connectedClients) {
                if (h.getGameRoom() == null && h.isJoined()) {
                    count++;
                }
            }
        }

        return count;
    }

    // Server kapatılır.
    public void shutdown() {
        running = false;
        ServerLogger.logShutdown();

        synchronized (connectedClients) {
            for (ClientHandler handler : new ArrayList<>(connectedClients)) {
                handler.disconnect();
            }
            connectedClients.clear();
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            ServerLogger.logError("Sunucu soketi kapatilamadi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                ServerLogger.logWarning("Gecersiz port: " + args[0]
                        + " - Varsayilan port kullaniliyor: " + DEFAULT_PORT);
            }
        }

        BackgammonServer server = new BackgammonServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "ShutdownHook"));

        server.start(port);
    }
}