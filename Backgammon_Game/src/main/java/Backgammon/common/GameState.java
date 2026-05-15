package Backgammon.common;
import java.io.Serializable;
import java.util.List;

// oyunun anlık durumunu tutar ve serverdan clienta gönderir
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Board board;
    private Player currentPlayer;
    private Player waitingPlayer;
    private Dice dice;
    private List<int[]> availableMoves;
    private boolean gameOver;
    private Player winner;
    private boolean diceRolled;
    private String statusMessage = "";
    private int initRollPlayer1;
    private int initRollPlayer2;
    private boolean mars;

    //boş oyun durumu nesnesi oluştur
    public GameState() {
    }

    //verilen parametrelere göre oyun bilgisi oluştur
    public GameState(Board board, Player currentPlayer, Player waitingPlayer, Dice dice) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.waitingPlayer = waitingPlayer;
        this.dice = dice;
    }

    //oyunu bitmiş olarak işaretler kazananı mars olanı belirler
    public void setGameOver(Player winner, boolean mars) {
        this.gameOver = true;
        this.winner = winner;
        this.mars = mars;
        this.statusMessage = mars
                ? winner.getUsername() + " MARS kazandi! (+2)"
                : winner.getUsername() + " kazandi!";
    }

    //oyun devam ediyor mu
    public boolean isOngoing() {
        return !gameOver;
    }

  //güncel tahtayı döndürür
    public Board getBoard() {
        return board;
    }

    //güncel tahtayı ayarlar
    public void setBoard(Board b) {
        this.board = b;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player p) {
        this.currentPlayer = p;
    }

    public Player getWaitingPlayer() {
        return waitingPlayer;
    }

    public void setWaitingPlayer(Player p) {
        this.waitingPlayer = p;
    }

    public Dice getDice() {
        return dice;
    }

    public void setDice(Dice d) {
        this.dice = d;
    }

    public List<int[]> getAvailableMoves() {
        return availableMoves;
    }

    public void setAvailableMoves(List<int[]> m) {
        this.availableMoves = m;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player w) {
        this.winner = w;
    }

    //zar atıldı mı
    public boolean isDiceRolled() {
        return diceRolled;
    }

    public void setDiceRolled(boolean v) {
        this.diceRolled = v;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String m) {
        this.statusMessage = m;
    }

    //birinci oyuncunun başlangıç zarını döndür
    public int getInitRollPlayer1() {
        return initRollPlayer1;
    }

    public void setInitRollPlayer1(int v) {
        this.initRollPlayer1 = v;
    }

    public int getInitRollPlayer2() {
        return initRollPlayer2;
    }

    public void setInitRollPlayer2(int v) {
        this.initRollPlayer2 = v;
    }

    //oyuncu mars oldu mu
    public boolean isMars() {
        return mars;
    }

    public void setMars(boolean m) {
        this.mars = m;
    }
}
