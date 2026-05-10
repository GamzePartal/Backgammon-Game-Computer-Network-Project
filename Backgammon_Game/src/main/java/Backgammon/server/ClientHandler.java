package Backgammon.server;

import Backgammon.common.GameMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private final Socket         socket;
    private final BackgammonServer server;
    private ObjectInputStream    inputStream;
    private ObjectOutputStream   outputStream;
    private final int            playerID;
    private String               username;
    private GameRoom             gameRoom;
    private volatile boolean     running;  // FIX: volatile ile diğer thread'lere görünür

    private int wins = 0;

    public ClientHandler(Socket socket, BackgammonServer server, int playerID) {
        this.socket   = socket;
        this.server   = server;
        this.playerID = playerID;
        this.running  = false;
        this.username = "Oyuncu" + playerID;
    }

    @Override
    public void run() {
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());

            // FIX 1: running bayrağını onClientReady'den ÖNCE set et
            running = true;
            ServerLogger.logNetwork("Oyuncu " + playerID + " baglandi: "
                    + socket.getInetAddress().getHostAddress());

            server.onClientReady(this);
            startListening();

        } catch (IOException e) {
            ServerLogger.logError("Oyuncu " + playerID + " akis hatasi: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void startListening() {
        while (running) {
            try {
                GameMessage message = (GameMessage) inputStream.readObject();
                if (message != null) {
                    handleMessage(message);
                }
            } catch (IOException e) {
                if (running) {
                    ServerLogger.logNetwork("Oyuncu " + playerID + " baglantisi kesildi.");
                }
                break;
            } catch (ClassNotFoundException e) {
                ServerLogger.logError("Bilinmeyen mesaj tipi: " + e.getMessage());
            }
        }
    }

    private void handleMessage(GameMessage message) {
        ServerLogger.log("Mesaj alindi [Oyuncu " + playerID + "]: " + message.getType());

        switch (message.getType()) {
            case PLAYER_JOIN:
                handlePlayerJoin(message);
                break;
            case ROLL_DICE:
                if (gameRoom != null) {
                    gameRoom.rollDice(playerID);
                }
                break;
            case MOVE_PIECE:
                if (gameRoom != null) {
                    gameRoom.processMove(message);
                }
                break;
            case REMATCH_REQUEST:
                if (gameRoom != null) {
                    gameRoom.handleRematchRequest(playerID);
                }
                break;
            default:
                ServerLogger.logWarning("Islenemeyen mesaj tipi: " + message.getType());
                break;
        }
    }

    private void handlePlayerJoin(GameMessage message) {
        if (message.getData() instanceof String) {
            this.username = (String) message.getData();
        }
        ServerLogger.log("Oyuncu " + playerID + " katildi: " + username);
    }

    public synchronized void sendMessage(GameMessage message) {
        try {
            if (outputStream != null && !socket.isClosed()) {
                outputStream.writeObject(message);
                outputStream.flush();
                outputStream.reset();
            }
        } catch (IOException e) {
            ServerLogger.logError("Oyuncu " + playerID + "'e mesaj gonderilemedi: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (!running) {
            return;
        }
        running = false;

        if (gameRoom != null && gameRoom.isActive()) {
            gameRoom.handleDisconnect(playerID);
        }

        server.removeClient(this);

        try {
            if (inputStream  != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            ServerLogger.logError("Baglanti kapatilamadi: " + e.getMessage());
        }

        ServerLogger.logNetwork("Oyuncu " + playerID + " baglantisi temizlendi.");
    }

    public void incrementWins()               { this.wins++; }
    public int  getWins()                     { return wins; }
    public int  getPlayerID()                 { return playerID; }
    public String getUsername()               { return username; }
    public GameRoom getGameRoom()             { return gameRoom; }
    public void setGameRoom(GameRoom r)       { this.gameRoom = r; }
    public boolean isRunning()                { return running; }
}