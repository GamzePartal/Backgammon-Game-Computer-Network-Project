package Backgammon.server;
import Backgammon.common.*;
import java.util.List;

public class GameRoom {

    private final String roomId;
    private final ClientHandler player1Handler; // Birinci oyuncunun ağ bağlantısını ve mesaj akışını yöneten handler
    private final ClientHandler player2Handler;     
    private Player player1;     
    private Player player2;  
    private Board board;
    private Dice dice; // Mevcut turdaki zar bilgisi
    private GameState gameState;  // Anlık oyun durumu ağa gönderilir
    private Player currentPlayer;    // şu an hamlesi olan oyuncu 
    private Player waitingPlayer; //hamle sırası bekleyen oyuncu
    private boolean active;  // oyun aktif mi

   
    public GameRoom(String roomId, ClientHandler player1Handler, ClientHandler player2Handler) {
        this.roomId = roomId;
        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;
        this.active = false;
    }

    
    // oyun başlatılır
    public void initGame() {
        ServerLogger.logGame(roomId, "Oyun başlatılıyor...");

        // Oyuncu nesnelerini oluştur 
        player1 = new Player(player1Handler.getPlayerID(), player1Handler.getUsername(), Player.WHITE);
        player2 = new Player(player2Handler.getPlayerID(),player2Handler.getUsername(), Player.BLACK);

        board = new Board();
        dice = new Dice();

        // Başlangıç sırasını belirle: iki zar at, yükseği başlar
        // Eşit gelirse tekrar atılır (basitlik için player1 hep başlar)  ????????????????????????????????????????
        currentPlayer = player1;
        waitingPlayer = player2;

        // GameState nesnesini oluştur
        gameState = new GameState(board, currentPlayer, waitingPlayer, dice);
        gameState.setStatusMessage(currentPlayer.getUsername() + " başlıyor!");

        // Oyuncuları bilgilendir
        active = true;
        broadcastState();

        // GAME_START mesajını gönder
        GameMessage startMsg = new GameMessage(MessageType.GAME_START, 0, gameState);
        player1Handler.sendMessage(startMsg);
        player2Handler.sendMessage(startMsg);

        ServerLogger.logGame(roomId, "Oyun başladı! " + player1.getUsername() + " vs " + player2.getUsername() + " (B)");
    }

    
    //oyuncu zar atmak istediğinde
    public void rollDice(int playerID) {
        
        if (!isPlayerTurn(playerID)) {
            sendErrorTo(playerID, "Şu an sizin sıranız değil!");
            return;
        }

        if (gameState.isDiceRolled()) {
            sendErrorTo(playerID, "Bu turda zaten zar attınız!");
            return;
        }

        // Zarları at
        int[] values = dice.roll();
        gameState.setDiceRolled(true);

        // Geçerli hamleleri hesapla
        List<int[]> moves = board.getAvailableMoves(dice, currentPlayer);
        gameState.setAvailableMoves(moves);

        // Çift bilgisini logla
        if (dice.isDoubles()) {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()+ " Çift zar attı: " + values[0] + "+" + values[0]);
        } else {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()+ " Zar attı: " + values[0] + "+" + values[1]);
        }

        // Hamle yoksa sırayı geç
        if (moves.isEmpty()) {
            gameState.setStatusMessage(currentPlayer.getUsername()+ " hamle yapamıyor, sıra geçiyor...");
            broadcastState();
            switchTurn();
            return;
        }

        gameState.setStatusMessage(currentPlayer.getUsername() + " hamle yapıyor...");
        broadcastState();
    }

   
    // gelen hamle geçerliyse tahta üzerinde uygulanır ve oyun bitti mi diye konrol et 
    //parametre olarak gameMesage nesnesi verilir çünkü hamle bilgilerini taşır from,to,dieValue gibi
    public void processMove(GameMessage msg) {
      
        if (!isPlayerTurn(msg.getSenderID())) {
            sendErrorTo(msg.getSenderID(), "Şu an sizin sıranız değil!");
            return;
        }

       
        if (!gameState.isDiceRolled()) {
            sendErrorTo(msg.getSenderID(), "Önce zar atmanız gerekiyor!");
            return;
        }

        int[] moveData = (int[]) msg.getData(); 
        int from = moveData[0]; // -1 = bar
        int to = moveData[1]; // -2 = bearing off
        int dieVal = moveData[2]; // Kullanılacak zar değeri

   
        if (!board.isValidMove(from, to, dice, currentPlayer)) {
            sendErrorTo(msg.getSenderID(), "Geçersiz hamle!");
            return;
        }

        // taş toplama hamlesi mi
        if (to == -2) {
            board.bearOff(from, currentPlayer, dieVal, dice);
            ServerLogger.logGame(roomId, currentPlayer.getUsername()+ " taşı topladı: " + (from + 1) + ". haneden");
        } else {
            // Normal hamle veya bar hamlesi
            Player opponent = getOpponent(currentPlayer);
            board.movePiece(from, to, currentPlayer, opponent, dice);
            ServerLogger.logGame(roomId, currentPlayer.getUsername()+ " " + (from == -1 ? "bar" : (from + 1)) + " -> " + (to + 1));
        }

        // kazandı mı
        if (board.checkWinner(currentPlayer)) {
            endGame(currentPlayer);
            return;
        }

        // Geçerli hamleleri güncelle
        List<int[]> remaining = board.getAvailableMoves(dice, currentPlayer);
        gameState.setAvailableMoves(remaining);

        // Tüm hamleler bittiyse sıra değişir
        if (dice.allUsed() || remaining.isEmpty()) {
            switchTurn();
        } else {
            gameState.setStatusMessage(currentPlayer.getUsername() + " devam ediyor (" + dice.getRemainingCount() + " hamle kaldı)");
            broadcastState();
        }
    }

  
    // sıranın diğer oyuncuya geçer
    public void switchTurn() {
        
        // Mevcut ve bekleyen oyuncuyu yer değiştir
        Player temp = currentPlayer;
        currentPlayer = waitingPlayer;
        waitingPlayer = temp;

        // Yeni tur için zarı sıfırla
        dice.reset();
        gameState.setDiceRolled(false);
        gameState.setCurrentPlayer(currentPlayer);
        gameState.setWaitingPlayer(waitingPlayer);
        gameState.setAvailableMoves(null);
        gameState.setStatusMessage(currentPlayer.getUsername() + "'in sırası - Zar atın!");

        ServerLogger.logGame(roomId, "Sıra geçti -> " + currentPlayer.getUsername());
        broadcastState();
    }

   
    // oyunu bitirir kazanını belirle ve iki oyuncuya da game over mesajı gönder
    public void endGame(Player winner) {
        gameState.setGameOver(winner);
        active = false;

        GameMessage overMsg = new GameMessage(MessageType.GAME_OVER, 0, gameState);
        player1Handler.sendMessage(overMsg);
        player2Handler.sendMessage(overMsg);

        ServerLogger.logGame(roomId, "OYUN BİTTİ! Kazanan: " + winner.getUsername());
    }

   
    //bir oyunucunun bağlantısı koptuğunda diğer oyuncu bilgilendirilir ve oyun sonlandırılır
    public void handleDisconnect(int disconnectedPlayerID) {
        active = false;
        ServerLogger.logGame(roomId, "Oyuncu " + disconnectedPlayerID + " bağlantıyı kesti.");

        GameMessage discMsg = new GameMessage(MessageType.PLAYER_DISCONNECT, disconnectedPlayerID, "Rakip bağlantıyı kesti.");

        if (player1Handler.getPlayerID() == disconnectedPlayerID) {
            player2Handler.sendMessage(discMsg);
        } else {
            player1Handler.sendMessage(discMsg);
        }
    }

    
    //her tahta değişikliğinden sonra çağır
    public void broadcastState() {
        
        gameState.setBoard(board);
        gameState.setCurrentPlayer(currentPlayer);
        gameState.setWaitingPlayer(waitingPlayer);
        gameState.setDice(dice);

        GameMessage updateMsg = new GameMessage(MessageType.BOARD_UPDATE, 0, gameState);
        player1Handler.sendMessage(updateMsg);
        player2Handler.sendMessage(updateMsg);
    }

    
    
      // Mesajı her iki oyuncuya da ilet gönderene de alana da
    public void broadcastChat(GameMessage msg) {
        player1Handler.sendMessage(msg);
        player2Handler.sendMessage(msg);
    }

    
    
    //belirtilen oyuncuya hata mesajı gönder
    private void sendErrorTo(int playerID, String errorText) {
        GameMessage errMsg = new GameMessage(MessageType.ERROR, 0, errorText);
        if (player1Handler.getPlayerID() == playerID) {
            player1Handler.sendMessage(errMsg);
        } else {
            player2Handler.sendMessage(errMsg);
        }
    }

    
    // oyuncunun rakibini döner
    private Player getOpponent(Player player) {
        return (player == player1) ? player2 : player1;
    }

    
    //verilen oyuncunun sırası mı
    public boolean isPlayerTurn(int playerID) {
        return currentPlayer != null && currentPlayer.getPlayerID() == playerID;
    }

    
    public String getRoomId() {
        return roomId;
    }

    //oyun odası aktif mi evetse oyun devam ediyo
    public boolean isActive() {
        return active;
    }
}
