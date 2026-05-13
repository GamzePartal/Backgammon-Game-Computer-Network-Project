package Backgammon.client;

import Backgammon.common.GameMessage;
import Backgammon.common.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BackgammonClient {

    private String serverIP;
    private int    serverPort;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream  inputStream;
    private volatile boolean listening;
    private ScreenManager screenManager;
    private int    playerID;
    private String username;

    public BackgammonClient(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.listening     = false;
    }

    public boolean connect(String ip, int port) {
        this.serverIP   = ip;
        this.serverPort = port;
        try {
            socket = new Socket(serverIP, serverPort);
            socket.setKeepAlive(true); // OS seviyesinde TCP keepalive — bağlantı düşmesini önler
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream  = new ObjectInputStream(socket.getInputStream());
            startListening();
            System.out.println("[ISTEMCI] Sunucuya baglandi: " + ip + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("[ISTEMCI] Baglanti hatasi: " + e.getMessage());
            return false;
        }
    }

    private void startListening() {
        listening = true;
        Thread t = new Thread(() -> {
            while (listening) {
                try {
                    GameMessage message = (GameMessage) inputStream.readObject();
                    if (message != null) handleServerMessage(message);
                } catch (IOException e) {
                    if (listening) {
                        System.err.println("[ISTEMCI] Sunucu baglantisi kesildi.");
                        screenManager.onDisconnected();
                    }
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("[ISTEMCI] Bilinmeyen mesaj: " + e.getMessage());
                }
            }
        }, "ServerListener");
        t.setDaemon(true);
        t.start();
    }

    public synchronized void sendMessage(GameMessage message) {
        try {
            if (outputStream != null && isConnected()) {
                outputStream.writeObject(message);
                outputStream.flush();
                outputStream.reset();
            }
        } catch (IOException e) {
            System.err.println("[ISTEMCI] Mesaj gonderilemedi: " + e.getMessage());
        }
    }

    public void sendRollDice() {
        System.out.println("[DEBUG] sendRollDice - playerID: " + playerID);
        sendMessage(new GameMessage(MessageType.ROLL_DICE, playerID));
    }

    public void sendMove(int from, int to, int dieVal) {
        System.out.println("[DEBUG] sendMove - playerID: " + playerID
                + " from:" + from + " to:" + to + " dieVal:" + dieVal);
        sendMessage(new GameMessage(MessageType.MOVE_PIECE, playerID,
                new int[]{from, to, dieVal}));
    }

    public void sendPlayerJoin(String username) {
        this.username = username;
        sendMessage(new GameMessage(MessageType.PLAYER_JOIN, playerID, username));
    }

    public void sendRematchRequest() {
        System.out.println("[DEBUG] sendRematchRequest - playerID: " + playerID);
        sendMessage(new GameMessage(MessageType.REMATCH_REQUEST, playerID));
    }

    private void handleServerMessage(GameMessage message) {
        screenManager.onMessageReceived(message);
    }

    public void disconnect() {
        listening = false;
        try {
            if (inputStream  != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[ISTEMCI] Kapatma hatasi: " + e.getMessage());
        }
        System.out.println("[ISTEMCI] Baglanti kapatildi.");
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public int    getPlayerID()        { return playerID; }
    public void   setPlayerID(int id)  { this.playerID = id; }
    public String getUsername()        { return username; }
    public String getServerIP()        { return serverIP; }
    public int    getServerPort()      { return serverPort; }
}