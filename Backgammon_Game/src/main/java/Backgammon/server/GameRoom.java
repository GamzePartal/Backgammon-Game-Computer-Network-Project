package Backgammon.server;
import Backgammon.common.*;
import java.util.List;
import java.util.Random;

//iki oyuncu arasındaki oyunu server tarafında yöneten ana class’tır
//Oyun başlatma, zar atma, hamle işleme, sıra değiştirme, oyun bitirme ve tekrar oynama işlemlerini yürütür.
public class GameRoom {

    private final String roomId;
    private final ClientHandler p1Handler;
    private final ClientHandler p2Handler;
    private Player player1, player2;
    private Board board;
    private Dice dice;
    private GameState gameState;
    private Player currentPlayer, waitingPlayer;
    private boolean active;
    private boolean p1WantsRematch, p2WantsRematch, rematchStarted;
    private static final Random RNG = new Random();

    public GameRoom(String roomId, ClientHandler p1Handler, ClientHandler p2Handler) {
        this.roomId = roomId;
        this.p1Handler = p1Handler;
        this.p2Handler = p2Handler;
    }

    //Yeni oyunu başlatır oyunculara renk verir, başlangıç zarlarını atar, 
    //ilk başlayacak oyuncuyu belirler ve clientlara GAME_START gönderir.
    public synchronized void initGame() {
        ServerLogger.logGame(roomId, "Oyun baslatiliyor...");
        p1WantsRematch = p2WantsRematch = rematchStarted = false;

        boolean p1IsWhite = RNG.nextBoolean();
        player1 = new Player(p1Handler.getPlayerID(), p1Handler.getUsername(), p1IsWhite ? Player.WHITE : Player.BLACK);
        player2 = new Player(p2Handler.getPlayerID(), p2Handler.getUsername(), p1IsWhite ? Player.BLACK : Player.WHITE);
        player1.setWins(p1Handler.getWins());
        player2.setWins(p2Handler.getWins());

        board = new Board();
        dice = new Dice();

        int roll1, roll2;
        do {
            roll1 = RNG.nextInt(6) + 1;
            roll2 = RNG.nextInt(6) + 1;
        } while (roll1 == roll2);

        currentPlayer = (roll1 > roll2) ? player1 : player2;
        waitingPlayer = (roll1 > roll2) ? player2 : player1;

        ServerLogger.logGame(roomId, "Baslangic: " + player1.getUsername() + "=" + roll1
                + " | " + player2.getUsername() + "=" + roll2
                + " -> " + currentPlayer.getUsername() + " basliyor!");

        gameState = new GameState(board, currentPlayer, waitingPlayer, dice);
        gameState.setStatusMessage(currentPlayer.getUsername() + " basliyor!");
        gameState.setInitRollPlayer1(p1IsWhite ? roll1 : roll2);
        gameState.setInitRollPlayer2(p1IsWhite ? roll2 : roll1);

        active = true;
        broadcast(new GameMessage(MessageType.GAME_START, 0, gameState));
        ServerLogger.logGame(roomId, "Oyun basladi: " + player1.getUsername() + " vs " + player2.getUsername());
    }

    //sıradaki oyuncunun zar atmasını sağlar
    //zar sonrası yapılabilecek hamleleri hesaplar hame yoksa sıra geçirir
    public synchronized void rollDice(int playerID) {
        if (!isPlayerTurn(playerID)) {
            sendError(playerID, "Su an sizin siraniz degil!");
            return;
        }
        if (gameState.isDiceRolled()) {
            sendError(playerID, "Bu turda zaten zar attiniz!");
            return;
        }

        int[] vals = dice.roll();
        gameState.setDiceRolled(true);
        List<int[]> moves = board.getAvailableMoves(dice, currentPlayer);
        gameState.setAvailableMoves(moves);

        ServerLogger.logGame(roomId, currentPlayer.getUsername() + " zar atti: " + vals[0] + "+" + vals[1]
                + (dice.isDoubles() ? " (CIFT)" : ""));

        if (moves.isEmpty()) {
            gameState.setStatusMessage(currentPlayer.getUsername() + " hamle yapamıyor (zar: "
                    + vals[0] + "-" + vals[1] + "), sıra geçiyor...");
            broadcastState();
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {
                }
                synchronized (GameRoom.this) {
                    if (active) {
                        switchTurn();
                    }
                }
            }, "DelayedSwitch-" + roomId) {
                {
                    setDaemon(true);
                }
            }.start();
            return;
        }
        gameState.setStatusMessage(currentPlayer.getUsername() + " hamle yapiyor...");
        broadcastState();
    }

    
    //oyuncunun gönderdiği hamleyi işer hamle geçerliyse tahtaya uygulanır
    public synchronized void processMove(GameMessage msg) {
        if (!isPlayerTurn(msg.getSenderID())) {
            sendError(msg.getSenderID(), "Su an sizin siraniz degil!");
            return;
        }
        if (!gameState.isDiceRolled()) {
            sendError(msg.getSenderID(), "Once zar atmaniz gerekiyor!");
            return;
        }

        int[] d = (int[]) msg.getData();
        int from = d[0], to = d[1];

        if (!board.isValidMove(from, to, dice, currentPlayer)) {
            sendError(msg.getSenderID(), "Gecersiz hamle!");
            return;
        }

        board.movePiece(from, to, currentPlayer, getOpponent(currentPlayer), dice);

        if (to == -2) {
            ServerLogger.logGame(roomId, currentPlayer.getUsername() + " tasi topladi: " + (from + 1));
        } else {
            ServerLogger.logGame(roomId, currentPlayer.getUsername() + " " + (from == -1 ? "bar" : (from + 1)) + " -> " + (to + 1));
        }

        if (board.checkWinner(currentPlayer)) {
            endGame(currentPlayer);
            return;
        }

        List<int[]> remaining = board.getAvailableMoves(dice, currentPlayer);
        gameState.setAvailableMoves(remaining);

        if (!dice.allUsed() && remaining.isEmpty()) {
            gameState.setStatusMessage(currentPlayer.getUsername() + " kalan zarlarla hamle yapamıyor, sıra geçiyor...");
            broadcastState();
            new Thread(() -> {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException ignored) {
                }
                synchronized (GameRoom.this) {
                    if (active) {
                        switchTurn();
                    }
                }
            }, "DelayedSwitch-" + roomId) {
                {
                    setDaemon(true);
                }
            }.start();
        } else if (dice.allUsed() || remaining.isEmpty()) {
            switchTurn();
        } else {
            gameState.setStatusMessage(currentPlayer.getUsername() + " devam ediyor (" + dice.getRemainingCount() + " hamle kaldi)");
            broadcastState();
        }
    }

    
    //sırayı diğer oyuncuya geçirir zarları sıfırlar oyun durumunu günceller ve clientlara gönderir
    public synchronized void switchTurn() {
        Player temp = currentPlayer;
        currentPlayer = waitingPlayer;
        waitingPlayer = temp;
        dice.reset();
        gameState.setDiceRolled(false);
        gameState.setCurrentPlayer(currentPlayer);
        gameState.setWaitingPlayer(waitingPlayer);
        gameState.setAvailableMoves(null);
        gameState.setStatusMessage(currentPlayer.getUsername() + "'in sirasi - Zar atin!");
        ServerLogger.logGame(roomId, "Sira gecti -> " + currentPlayer.getUsername());
        broadcastState();
    }

    //kazananı belirler, mars durumuna bakar,galibiyet sayısını arttır,gameover mesajı gönderir
    public synchronized void endGame(Player winner) {
        Player loser = getOpponent(winner);
        boolean isMars = loser.getPiecesBorneOff() == 0;
        int winsToAdd = isMars ? 2 : 1;

        ClientHandler wh = (winner == player1) ? p1Handler : p2Handler;
        for (int i = 0; i < winsToAdd; i++) {
            wh.incrementWins();
        }
        winner.setWins(wh.getWins());

        gameState.setGameOver(winner, isMars);
        active = false;
        broadcast(new GameMessage(MessageType.GAME_OVER, 0, gameState));
        ServerLogger.logGame(roomId, "OYUN BITTI! " + (isMars ? "MARS! " : "") + "Kazanan: "
                + winner.getUsername() + " (+" + winsToAdd + ", toplam: " + wh.getWins() + ")");
    }

    
    //oyuncunun tekrar oynama istediğini alır iğer oyuncu da kabul ederse yeni oyun başlatılır
    public synchronized void handleRematchRequest(int playerID) {
        boolean isP1 = p1Handler.getPlayerID() == playerID;
        if (isP1) {
            p1WantsRematch = true;
            if (!p2WantsRematch) {
                p2Handler.sendMessage(new GameMessage(MessageType.WAITING, 0, "Rakip tekrar oynamak istiyor..."));
            }
        } else {
            p2WantsRematch = true;
            if (!p1WantsRematch) {
                p1Handler.sendMessage(new GameMessage(MessageType.WAITING, 0, "Rakip tekrar oynamak istiyor..."));
            }
        }
        if (p1WantsRematch && p2WantsRematch && !rematchStarted) {
            rematchStarted = true;
            new Thread(this::initGame, "Rematch-" + roomId) {
                {
                    setDaemon(true);
                }
            }.start();
        }
    }

    
    //bir oyuncu bağlantıyı keserse rakibe bilgi verilir ve oyun kapatılır
    public synchronized void handleDisconnect(int disconnectedID) {
        active = false;
        GameMessage msg = new GameMessage(MessageType.PLAYER_DISCONNECT, disconnectedID, "Rakip baglantiyi kesti.");
        (p1Handler.getPlayerID() == disconnectedID ? p2Handler : p1Handler).sendMessage(msg);
    }

    //güncel gamState bilgisini iki oyuncuyada gönderir
    public synchronized void broadcastState() {
        gameState.setBoard(board);
        gameState.setCurrentPlayer(currentPlayer);
        gameState.setWaitingPlayer(waitingPlayer);
        gameState.setDice(dice);
        broadcast(new GameMessage(MessageType.BOARD_UPDATE, 0, gameState));
    }

    //verilen mesajı iki oyuncuya birden gönderir
    private void broadcast(GameMessage msg) {
        p1Handler.sendMessage(msg);
        p2Handler.sendMessage(msg);
    }

    //belirtilen oyuncuya hata mesajı gönderilir
    private void sendError(int pid, String text) {
        (p1Handler.getPlayerID() == pid ? p1Handler : p2Handler).sendMessage(new GameMessage(MessageType.ERROR, 0, text));
    }

    //verilen oyuncunun rakibini döner
    private Player getOpponent(Player p) {
        return (p == player1) ? player2 : player1;
    }

    //verilen oyuncu idsi sıradaki oyuncuya mı ait
    public boolean isPlayerTurn(int pid) {
        return currentPlayer != null && currentPlayer.getPlayerID() == pid;
    }

    public String getRoomId() {
        return roomId;
    }

    //oyun odası aktif mi
    public boolean isActive() {
        return active;
    }
}
