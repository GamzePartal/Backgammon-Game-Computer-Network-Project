package Backgammon.client;
import Backgammon.common.GameMessage;
import Backgammon.common.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class BackgammonClient {

    private String serverIP; //bağlanılacak serverin ıpsini tutar
    private int serverPort; //serverin port numarasını tutar
    private Socket socket;  //server ile client arasında bağlantı kurar 
    private ObjectOutputStream outputStream; //servera nesne gönderir
    private ObjectInputStream inputStream; // serverdan nesne okur
    private volatile boolean listening;  //volalite sayesinde treadler karışmaz anlık güncellenir
    private ScreenManager screenManager;
    private int playerID; //oyuncuya server tarafından atanan id
    private String username;

    public BackgammonClient(ScreenManager screenManager) {
        this.screenManager = screenManager;   //serverdan gelen mesajları client bu ekrana yönlendirir cleint ne işlem yapacağını bilmez
        this.listening = false;  //servera henüz bağlanılmadı
    }

    //client servere bağlanırsa true döner
    public boolean connect(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
        try {
            socket = new Socket(serverIP, serverPort); //bağlantı kuruluyor socket sayesinde
            socket.setKeepAlive(true); // bağlantı boşta kaldığında kapanmaz
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush(); //streamin hemen göndrilmesini sağlar  inputla outputstream birbirini beklemesin diye
            inputStream = new ObjectInputStream(socket.getInputStream());
            startListening(); //bağlantı kurulunca serverdan gelen mesajları dinler
            System.out.println("[ISTEMCI] Sunucuya baglandi: " + ip + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("[ISTEMCI] Baglanti hatasi: " + e.getMessage());
            return false;
        }
    }

    //serverdan gelen mesaj dinlenilir private çünkü başka classta çağırmaya gerek yok
    private void startListening() {
        listening = true;
        Thread t = new Thread(() -> {
            while (listening) {
                try {
                    GameMessage message = (GameMessage) inputStream.readObject();
                    if (message != null) {
                        handleServerMessage(message);
                    }
                } catch (IOException e) {
                    if (listening) {
                        System.err.println("[ISTEMCI] Sunucu baglantisi kesildi.");
                        screenManager.onDisconnected();
                    }
                    break;
                } catch (ClassNotFoundException e) { //readobject ile gelen mesaj client tarafından tanınmazsa
                    System.err.println("[ISTEMCI] Bilinmeyen mesaj: " + e.getMessage());
                }
            }
        }, "ServerListener");
        t.setDaemon(true);
        t.start(); //burdan sonra devamlı client serverdan mesaj bekler 
    }

    //servera mesaj göndeririz
    //synchronized çünkü aynı anda sadece bir thread bu methodu kullansın karışıklık olmasın 
    public synchronized void sendMessage(GameMessage message) {
        try {
            if (outputStream != null && isConnected()) {
                outputStream.writeObject(message);
                outputStream.flush();
                outputStream.reset(); //aynı nesne gönderilince eski hali gelmesin diye cache temizlenir
            }
        } catch (IOException e) {
            System.err.println("[ISTEMCI] Mesaj gonderilemedi: " + e.getMessage());
        }
    }

    //zar atma butonuna bastığında servera mesaj gönderilir server zar atar 
    public void sendRollDice() {
        System.out.println("[DEBUG] sendRollDice - playerID: " + playerID);
        sendMessage(new GameMessage(MessageType.ROLL_DICE, playerID));
    }

    //oyuncu hamle yaptığında servera gönderir
    public void sendMove(int from, int to, int dieVal) {
        System.out.println("[DEBUG] sendMove - playerID: " + playerID + " from:" + from + " to:" + to + " dieVal:" + dieVal);
        sendMessage(new GameMessage(MessageType.MOVE_PIECE, playerID, new int[]{from, to, dieVal}));
    }

    public void sendPlayerJoin(String username) {
        this.username = username;
        sendMessage(new GameMessage(MessageType.PLAYER_JOIN, playerID, username));
    }

    public void sendRematchRequest() {
        System.out.println("[DEBUG] sendRematchRequest - playerID: " + playerID);
        sendMessage(new GameMessage(MessageType.REMATCH_REQUEST, playerID));
    }

    //serverdan gelen mesajı screenmanagera aktarır
    private void handleServerMessage(GameMessage message) {
        screenManager.onMessageReceived(message);
    }

    //client bağlantısını kapatır
    public void disconnect() {
        listening = false;
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
            System.err.println("[ISTEMCI] Kapatma hatasi: " + e.getMessage());
        }
        System.out.println("[ISTEMCI] Baglanti kapatildi.");
    }

    //client bağlı mı?
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int id) {
        this.playerID = id;
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
