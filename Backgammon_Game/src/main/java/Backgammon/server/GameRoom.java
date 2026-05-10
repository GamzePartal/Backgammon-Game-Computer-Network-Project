package Backgammon.server;

import Backgammon.common.*;
import java.util.List;
import java.util.Random;

public class GameRoom {

    private final String        roomId;
    private final ClientHandler player1Handler;
    private final ClientHandler player2Handler;
    private Player    player1;
    private Player    player2;
    private Board     board;
    private Dice      dice;
    private GameState gameState;
    private Player    currentPlayer;
    private Player    waitingPlayer;
    private boolean   active;

    private boolean player1WantsRematch = false;
    private boolean player2WantsRematch = false;
    private boolean rematchStarted      = false;

    private static final Random RNG = new Random();

    public GameRoom(String roomId,
                    ClientHandler player1Handler,
                    ClientHandler player2Handler) {
        this.roomId         = roomId;
        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;
        this.active         = false;
    }

    public synchronized void initGame() {
        ServerLogger.logGame(roomId, "Oyun baslatiliyor...");

        player1WantsRematch = false;
        player2WantsRematch = false;
        rematchStarted      = false; // FIX 2: yeni oyun için bayrağı sıfırla

        // Renk atamasını her oyunda rastgele belirle
        boolean player1IsWhite = RNG.nextBoolean();
        int color1 = player1IsWhite ? Player.WHITE : Player.BLACK;
        int color2 = player1IsWhite ? Player.BLACK : Player.WHITE;

        player1 = new Player(player1Handler.getPlayerID(),
                             player1Handler.getUsername(), color1);
        player1.setWins(player1Handler.getWins());

        player2 = new Player(player2Handler.getPlayerID(),
                             player2Handler.getUsername(), color2);
        player2.setWins(player2Handler.getWins());

        board = new Board();
        dice  = new Dice();

        int roll1, roll2;
        do {
            roll1 = RNG.nextInt(6) + 1;
            roll2 = RNG.nextInt(6) + 1;
        } while (roll1 == roll2);

        if (roll1 > roll2) {
            currentPlayer = player1;
            waitingPlayer = player2;
        } else {
            currentPlayer = player2;
            waitingPlayer = player1;
        }

        ServerLogger.logGame(roomId, "Baslangic zari: "
                + player1.getUsername() + "=" + roll1
                + " | " + player2.getUsername() + "=" + roll2
                + " -> " + currentPlayer.getUsername() + " basliyor!");

        gameState = new GameState(board, currentPlayer, waitingPlayer, dice);
        gameState.setStatusMessage(currentPlayer.getUsername() + " basliyor!");

     
        // Böylece client tarafında beyaz/siyah eşleştirmesi doğru yapılır
        Player whitePlayer = player1IsWhite ? player1 : player2;
        Player blackPlayer = player1IsWhite ? player2 : player1;
        int rollForWhite   = player1IsWhite ? roll1 : roll2;
        int rollForBlack   = player1IsWhite ? roll2 : roll1;

        gameState.setInitRollPlayer1(rollForWhite); // Player1 slot = WHITE oyuncunun zarı
        gameState.setInitRollPlayer2(rollForBlack); // Player2 slot = BLACK oyuncunun zarı

        active = true;

        GameMessage startMsg = new GameMessage(MessageType.GAME_START, 0, gameState);
        player1Handler.sendMessage(startMsg);
        player2Handler.sendMessage(startMsg);

        ServerLogger.logGame(roomId, "Oyun basladi! "
                + player1.getUsername() + " vs " + player2.getUsername());
    }

    public synchronized void rollDice(int playerID) {
        if (!isPlayerTurn(playerID)) {
            sendErrorTo(playerID, "Su an sizin siraniz degil!");
            return;
        }
        if (gameState.isDiceRolled()) {
            sendErrorTo(playerID, "Bu turda zaten zar attiniz!");
            return;
        }

        int[] values = dice.roll();
        gameState.setDiceRolled(true);

        List<int[]> moves = board.getAvailableMoves(dice, currentPlayer);
        gameState.setAvailableMoves(moves);

        if (dice.isDoubles()) {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()
                    + " Cift zar atti: " + values[0] + "+" + values[0]);
        } else {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()
                    + " Zar atti: " + values[0] + "+" + values[1]);
        }

        if (moves.isEmpty()) {
            gameState.setStatusMessage(currentPlayer.getUsername()
                    + " hamle yapamiyor, sira geciyor...");
            broadcastState();
            switchTurn();
            return;
        }

        gameState.setStatusMessage(currentPlayer.getUsername() + " hamle yapiyor...");
        broadcastState();
    }

    public synchronized void processMove(GameMessage msg) {
        if (!isPlayerTurn(msg.getSenderID())) {
            sendErrorTo(msg.getSenderID(), "Su an sizin siraniz degil!");
            return;
        }
        if (!gameState.isDiceRolled()) {
            sendErrorTo(msg.getSenderID(), "Once zar atmaniz gerekiyor!");
            return;
        }

        int[] moveData = (int[]) msg.getData();
        int from = moveData[0];
        int to   = moveData[1];

        if (!board.isValidMove(from, to, dice, currentPlayer)) {
            sendErrorTo(msg.getSenderID(), "Gecersiz hamle!");
            return;
        }

        Player opponent = getOpponent(currentPlayer);
        board.movePiece(from, to, currentPlayer, opponent, dice);

        if (to == -2) {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()
                    + " tasi topladi: " + (from + 1) + ". haneden");
        } else {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()
                    + " " + (from == -1 ? "bar" : (from + 1))
                    + " -> " + (to + 1));
        }

        if (board.checkWinner(currentPlayer)) {
            endGame(currentPlayer);
            return;
        }

        List<int[]> remaining = board.getAvailableMoves(dice, currentPlayer);
        gameState.setAvailableMoves(remaining);

        if (dice.allUsed() || remaining.isEmpty()) {
            switchTurn();
        } else {
            gameState.setStatusMessage(currentPlayer.getUsername()
                    + " devam ediyor (" + dice.getRemainingCount() + " hamle kaldi)");
            broadcastState();
        }
    }

    public synchronized void switchTurn() {
        Player temp   = currentPlayer;
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

    private boolean checkMars(Player loser) {
        if (loser.getPiecesBorneOff() == 0) {
            ServerLogger.logGame(roomId, "Mars kontrolu: " + loser.getUsername()
                    + " hic tas toplayamamis -> MARS");
            return true;
        }
        ServerLogger.logGame(roomId, "Mars kontrolu: " + loser.getUsername()
                + " en az 1 tas toplamis -> mars yok");
        return false;
    }

    public synchronized void endGame(Player winner) {
        Player loser  = getOpponent(winner);
        boolean isMars = checkMars(loser);
        int winsToAdd  = isMars ? 2 : 1;

        ClientHandler winnerHandler = (winner == player1) ? player1Handler : player2Handler;
        for (int i = 0; i < winsToAdd; i++) {
            winnerHandler.incrementWins();
        }
        winner.setWins(winnerHandler.getWins());

        gameState.setGameOver(winner, isMars);
        active = false;

        GameMessage overMsg = new GameMessage(MessageType.GAME_OVER, 0, gameState);
        player1Handler.sendMessage(overMsg);
        player2Handler.sendMessage(overMsg);

        if (isMars) {
            ServerLogger.logGame(roomId, "OYUN BITTI! MARS! Kazanan: "
                    + winner.getUsername()
                    + " (+2 galibiyet, toplam: " + winnerHandler.getWins() + ")");
        } else {
            ServerLogger.logGame(roomId, "OYUN BITTI! Kazanan: "
                    + winner.getUsername()
                    + " (+1 galibiyet, toplam: " + winnerHandler.getWins() + ")");
        }
    }

    public synchronized void handleRematchRequest(int playerID) {
        if (player1Handler.getPlayerID() == playerID) {
            player1WantsRematch = true;
            ServerLogger.logGame(roomId, player1Handler.getUsername() + " rematch istiyor.");
            // Rakibe bildirim — sadece o daha istemediyse gönder
            if (!player2WantsRematch) {
                player2Handler.sendMessage(new GameMessage(
                        MessageType.WAITING, 0, "Rakip tekrar oynamak istiyor, bekleniyor..."));
            }
        } else {
            player2WantsRematch = true;
            ServerLogger.logGame(roomId, player2Handler.getUsername() + " rematch istiyor.");
            if (!player1WantsRematch) {
                player1Handler.sendMessage(new GameMessage(
                        MessageType.WAITING, 0, "Rakip tekrar oynamak istiyor, bekleniyor..."));
            }
        }

        // FIX 2: rematchStarted bayrağı ile çift başlatmayı önle
        if (player1WantsRematch && player2WantsRematch && !rematchStarted) {
            rematchStarted = true;
            ServerLogger.logGame(roomId, "Her iki oyuncu da rematch istedi. Yeni oyun basliyor...");
            Thread t = new Thread(this::initGame, "Rematch-" + roomId);
            t.setDaemon(true);
            t.start();
        }
    }

    public synchronized void handleDisconnect(int disconnectedPlayerID) {
        active = false;
        ServerLogger.logGame(roomId, "Oyuncu " + disconnectedPlayerID + " baglantiyi kesti.");

        GameMessage discMsg = new GameMessage(
                MessageType.PLAYER_DISCONNECT,
                disconnectedPlayerID,
                "Rakip baglantiyi kesti."
        );

        if (player1Handler.getPlayerID() == disconnectedPlayerID) {
            player2Handler.sendMessage(discMsg);
        } else {
            player1Handler.sendMessage(discMsg);
        }
    }

    // FIX 1: synchronized eklendi — farklı thread'lerden çağrılıyor
    public synchronized void broadcastState() {
        gameState.setBoard(board);
        gameState.setCurrentPlayer(currentPlayer);
        gameState.setWaitingPlayer(waitingPlayer);
        gameState.setDice(dice);

        GameMessage updateMsg = new GameMessage(MessageType.BOARD_UPDATE, 0, gameState);
        player1Handler.sendMessage(updateMsg);
        player2Handler.sendMessage(updateMsg);
    }

    private void sendErrorTo(int playerID, String errorText) {
        GameMessage errMsg = new GameMessage(MessageType.ERROR, 0, errorText);
        if (player1Handler.getPlayerID() == playerID) {
            player1Handler.sendMessage(errMsg);
        } else {
            player2Handler.sendMessage(errMsg);
        }
    }

    private Player getOpponent(Player player) {
        return (player == player1) ? player2 : player1;
    }

    public boolean isPlayerTurn(int playerID) {
        return currentPlayer != null
                && currentPlayer.getPlayerID() == playerID;
    }

    public String  getRoomId() { return roomId; }
    public boolean isActive()  { return active; }
}