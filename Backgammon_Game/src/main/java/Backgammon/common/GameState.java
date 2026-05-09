package Backgammon.common;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Board        board;
    private Player       currentPlayer;
    private Player       waitingPlayer;
    private Dice         dice;
    private List<int[]>  availableMoves;
    private boolean      gameOver;
    private Player       winner;
    private boolean      diceRolled;
    private String       statusMessage;

    // Başlangıç zar atışı sonuçları — sadece GAME_START mesajında dolu gelir
    private int initRollPlayer1 = 0; // beyaz oyuncunun başlangıç zarı
    private int initRollPlayer2 = 0; // siyah oyuncunun başlangıç zarı

    // Mars: kazanan oyuncu kazanırken kaybeden hiç taş toplamamışsa true
    // Mars olduğunda kazanan +2 galibiyet alır
    private boolean mars = false;

    

    public GameState() {
        this.gameOver      = false;
        this.diceRolled    = false;
        this.statusMessage = "";
    }

    public GameState(Board board, Player currentPlayer,
                     Player waitingPlayer, Dice dice) {
        this.board         = board;
        this.currentPlayer = currentPlayer;
        this.waitingPlayer = waitingPlayer;
        this.dice          = dice;
        this.gameOver      = false;
        this.diceRolled    = false;
        this.statusMessage = "";
    }


    public void setGameOver(Player winner, boolean mars) {
        this.gameOver      = true;
        this.winner        = winner;
        this.mars          = mars;
        this.statusMessage = mars
                ? winner.getUsername() + " MARS kazandı! (+2)"
                : winner.getUsername() + " kazandı!";
    }

    public boolean isOngoing() { return !gameOver; }

    

    public Board getBoard()                          { return board; }
    public void  setBoard(Board board)               { this.board = board; }

    public Player getCurrentPlayer()                 { return currentPlayer; }
    public void   setCurrentPlayer(Player p)         { this.currentPlayer = p; }

    public Player getWaitingPlayer()                 { return waitingPlayer; }
    public void   setWaitingPlayer(Player p)         { this.waitingPlayer = p; }

    public Dice  getDice()                           { return dice; }
    public void  setDice(Dice dice)                  { this.dice = dice; }

    public List<int[]> getAvailableMoves()           { return availableMoves; }
    public void setAvailableMoves(List<int[]> moves) { this.availableMoves = moves; }

    public boolean isGameOver()                      { return gameOver; }

    public Player getWinner()                        { return winner; }
    public void   setWinner(Player winner)           { this.winner = winner; }

    public boolean isDiceRolled()                    { return diceRolled; }
    public void    setDiceRolled(boolean v)          { this.diceRolled = v; }

    public String getStatusMessage()                 { return statusMessage; }
    public void   setStatusMessage(String msg)       { this.statusMessage = msg; }

    public int  getInitRollPlayer1()                 { return initRollPlayer1; }
    public void setInitRollPlayer1(int v)            { this.initRollPlayer1 = v; }

    public int  getInitRollPlayer2()                 { return initRollPlayer2; }
    public void setInitRollPlayer2(int v)            { this.initRollPlayer2 = v; }

    /** Mars durumu: kaybeden hiç taş toplamamışsa true. */
    public boolean isMars()                          { return mars; }
    public void    setMars(boolean mars)             { this.mars = mars; }
}