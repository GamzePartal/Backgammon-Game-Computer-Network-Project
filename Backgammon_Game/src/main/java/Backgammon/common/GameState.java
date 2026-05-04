
package Backgammon.common;
import java.io.Serializable;
import java.util.List;

public class GameState  implements Serializable{

    private static final long serialVersionUID = 1L;
    private Board board;
    private Player currentPlayer;  // Şu anda hamlesi olan oyuncunun bilgileri
    private Player waitingPlayer; 
    private Dice dice; 
    private List<int[]> availableMoves; 
    private boolean gameOver;
    private Player winner;
    private boolean diceRolled;
    private String statusMessage; //sunucu tarafından gönderilen mesaj
 
    
    public GameState() {
        this.gameOver = false;
        this.diceRolled = false;
        this.statusMessage = "";
    }
 
    
    public GameState(Board board, Player currentPlayer, Player waitingPlayer, Dice dice) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.waitingPlayer = waitingPlayer;
        this.dice = dice;
        this.gameOver = false;
        this.diceRolled = false;
        this.statusMessage = "";
    }
 

    //oyunun sona erdiğini ve kazanını belirtir
    public void setGameOver(Player winner) {
        this.gameOver = true;
        this.winner = winner;
        this.statusMessage = winner.getUsername() + " wonnnn!";
    }
 

    // oyun devam ediyor mu?
    public boolean isOngoing() {
        return !gameOver;
    }
 
    
    public Board getBoard() {
        return board;
    }
 
   
    public void setBoard(Board board) {
        this.board = board;
    }
 
   
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
 
    
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
 
   
    public Player getWaitingPlayer() {
        return waitingPlayer;
    }
 
    
    public void setWaitingPlayer(Player waitingPlayer) {
        this.waitingPlayer = waitingPlayer;
    }
 
   
    public Dice getDice() {
        return dice;
    }
 
    
    public void setDice(Dice dice) {
        this.dice = dice;
    }
 
    
    public List<int[]> getAvailableMoves() {
        return availableMoves;
    }
 
    
    public void setAvailableMoves(List<int[]> availableMoves) {
        this.availableMoves = availableMoves;
    }
 
    
    public boolean isGameOver() {
        return gameOver;
    }
 
   
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
 
    
    public Player getWinner() {
        return winner;
    }
 
  
    public void setWinner(Player winner) {
        this.winner = winner;
    }
 
   
    public boolean isDiceRolled() {
        return diceRolled;
    }
 
    
    public void setDiceRolled(boolean diceRolled) {
        this.diceRolled = diceRolled;
    }
 
   
    public String getStatusMessage() {
        return statusMessage;
    }
 
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
