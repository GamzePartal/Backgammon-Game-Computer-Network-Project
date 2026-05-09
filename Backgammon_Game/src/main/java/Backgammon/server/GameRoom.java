package Backgammon.server;

import Backgammon.common.*;
import java.util.List;
import java.util.Random;

public class GameRoom {

    private final String roomId;
    private final ClientHandler player1Handler;
    private final ClientHandler player2Handler;
    private Player player1;
    private Player player2;
    private Board board;
    private Dice dice;
    private GameState gameState;
    private Player currentPlayer;
    private Player waitingPlayer;
    private boolean active;

    private boolean player1WantsRematch = false;
    private boolean player2WantsRematch = false;

    private static final Random RNG = new Random();

    public GameRoom(String roomId,
            ClientHandler player1Handler,
            ClientHandler player2Handler) {
        this.roomId = roomId;
        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;
        this.active = false;
    }

    public void initGame() {
        ServerLogger.logGame(roomId, "Oyun başlatılıyor...");

        player1WantsRematch = false;
        player2WantsRematch = false;

        player1 = new Player(player1Handler.getPlayerID(),
                player1Handler.getUsername(), Player.WHITE);
        player1.setWins(player1Handler.getWins());

        player2 = new Player(player2Handler.getPlayerID(),
                player2Handler.getUsername(), Player.BLACK);
        player2.setWins(player2Handler.getWins());

        board = new Board();
        dice = new Dice();

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

        ServerLogger.logGame(roomId, "Başlangıç zarı: "
                + player1.getUsername() + "=" + roll1
                + " | " + player2.getUsername() + "=" + roll2
                + " → " + currentPlayer.getUsername() + " başlıyor!");

        gameState = new GameState(board, currentPlayer, waitingPlayer, dice);
        gameState.setInitRollPlayer1(roll1);
        gameState.setInitRollPlayer2(roll2);
        gameState.setStatusMessage(currentPlayer.getUsername() + " başlıyor!");

        active = true;

        GameMessage startMsg = new GameMessage(MessageType.GAME_START, 0, gameState);
        player1Handler.sendMessage(startMsg);
        player2Handler.sendMessage(startMsg);

        ServerLogger.logGame(roomId, "Oyun başladı! "
                + player1.getUsername() + " (B) vs "
                + player2.getUsername() + " (S)");
    }

    public void rollDice(int playerID) {
        if (!isPlayerTurn(playerID)) {
            sendErrorTo(playerID, "Şu an sizin sıranız değil!");
            return;
        }
        if (gameState.isDiceRolled()) {
            sendErrorTo(playerID, "Bu turda zaten zar attınız!");
            return;
        }

        int[] values = dice.roll();
        gameState.setDiceRolled(true);

        List<int[]> moves = board.getAvailableMoves(dice, currentPlayer);
        gameState.setAvailableMoves(moves);

        if (dice.isDoubles()) {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()
                    + " Çift zar attı: " + values[0] + "+" + values[0]);
        } else {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()
                    + " Zar attı: " + values[0] + "+" + values[1]);
        }

        if (moves.isEmpty()) {
            gameState.setStatusMessage(currentPlayer.getUsername()
                    + " hamle yapamıyor, sıra geçiyor...");
            broadcastState();
            switchTurn();
            return;
        }

        gameState.setStatusMessage(currentPlayer.getUsername() + " hamle yapıyor...");
        broadcastState();
    }

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
        int from = moveData[0];
        int to = moveData[1];

        if (!board.isValidMove(from, to, dice, currentPlayer)) {
            sendErrorTo(msg.getSenderID(), "Geçersiz hamle!");
            return;
        }

        Player opponent = getOpponent(currentPlayer);
        board.movePiece(from, to, currentPlayer, opponent, dice);

        if (to == -2) {
            ServerLogger.logGame(roomId, currentPlayer.getUsername()
                    + " taşı topladı: " + (from + 1) + ". haneden");
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
                    + " devam ediyor (" + dice.getRemainingCount() + " hamle kaldı)");
            broadcastState();
        }
    }

    public void switchTurn() {
        Player temp = currentPlayer;
        currentPlayer = waitingPlayer;
        waitingPlayer = temp;

        dice.reset();
        gameState.setDiceRolled(false);
        gameState.setCurrentPlayer(currentPlayer);
        gameState.setWaitingPlayer(waitingPlayer);
        gameState.setAvailableMoves(null);
        gameState.setStatusMessage(currentPlayer.getUsername() + "'in sırası - Zar atın!");

        ServerLogger.logGame(roomId, "Sıra geçti -> " + currentPlayer.getUsername());
        broadcastState();
    }

    private boolean checkMars(Player loser) {
        // Bar'da taş varsa zaten mars
        if (loser.hasBarPiece()) {
            ServerLogger.logGame(roomId, "Mars kontrolü: " + loser.getUsername()
                    + " barda taşı var → MARS");
            return true;
        }

        Point[] points = board.getPoints();
        int loserColor = loser.getColor();

        if (loserColor == Player.WHITE) {
            // WHITE'ın rakip evi: BLACK'in evi = indeks 18-23
            for (int i = 18; i < 24; i++) {
                if (points[i].getOwner() == loserColor && points[i].getCount() > 0) {
                    ServerLogger.logGame(roomId, "Mars kontrolü: " + loser.getUsername()
                            + " rakip evde (" + (i + 1) + ". hane) taşı var → MARS");
                    return true;
                }
            }
        } else {
            // BLACK'ın rakip evi: WHITE'ın evi = indeks 0-5
            for (int i = 0; i < 6; i++) {
                if (points[i].getOwner() == loserColor && points[i].getCount() > 0) {
                    ServerLogger.logGame(roomId, "Mars kontrolü: " + loser.getUsername()
                            + " rakip evde (" + (i + 1) + ". hane) taşı var → MARS");
                    return true;
                }
            }
        }

        ServerLogger.logGame(roomId, "Mars kontrolü: " + loser.getUsername()
                + " mars yok (kendi evine taşımış)");
        return false;
    }

    public void endGame(Player winner) {
        Player loser = getOpponent(winner);
        boolean isMars = checkMars(loser);
        int winsToAdd = isMars ? 2 : 1;

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
            ServerLogger.logGame(roomId, "OYUN BİTTİ! MARS! Kazanan: "
                    + winner.getUsername()
                    + " (+2 galibiyet, toplam: " + winnerHandler.getWins() + ")");
        } else {
            ServerLogger.logGame(roomId, "OYUN BİTTİ! Kazanan: "
                    + winner.getUsername()
                    + " (+1 galibiyet, toplam: " + winnerHandler.getWins() + ")");
        }
    }

    public synchronized void handleRematchRequest(int playerID) {
        if (player1Handler.getPlayerID() == playerID) {
            player1WantsRematch = true;
            ServerLogger.logGame(roomId, player1Handler.getUsername() + " rematch istiyor.");
            GameMessage waitMsg = new GameMessage(MessageType.WAITING, 0,
                    "Rakip tekrar oynamak istiyor, bekleniyor...");
            player2Handler.sendMessage(waitMsg);
        } else {
            player2WantsRematch = true;
            ServerLogger.logGame(roomId, player2Handler.getUsername() + " rematch istiyor.");
            GameMessage waitMsg = new GameMessage(MessageType.WAITING, 0,
                    "Rakip tekrar oynamak istiyor, bekleniyor...");
            player1Handler.sendMessage(waitMsg);
        }

        if (player1WantsRematch && player2WantsRematch) {
            ServerLogger.logGame(roomId, "Her iki oyuncu da rematch istedi. Yeni oyun başlıyor...");
            Thread t = new Thread(this::initGame, "Rematch-" + roomId);
            t.setDaemon(true);
            t.start();
        }
    }

    public void handleDisconnect(int disconnectedPlayerID) {
        active = false;
        ServerLogger.logGame(roomId, "Oyuncu " + disconnectedPlayerID + " bağlantıyı kesti.");

        GameMessage discMsg = new GameMessage(
                MessageType.PLAYER_DISCONNECT,
                disconnectedPlayerID,
                "Rakip bağlantıyı kesti."
        );

        if (player1Handler.getPlayerID() == disconnectedPlayerID) {
            player2Handler.sendMessage(discMsg);
        } else {
            player1Handler.sendMessage(discMsg);
        }
    }

    public void broadcastState() {
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

    public String getRoomId() {
        return roomId;
    }

    public boolean isActive() {
        return active;
    }
}
