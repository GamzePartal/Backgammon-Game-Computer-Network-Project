package Backgammon.common;
import java.io.Serializable;

//oyuncunun bilgilerini ve oyun içindeki taş durumunu tutar
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int WHITE = 1;
    public static final int BLACK = 2;

    private final int    playerID;
    private final String username;
    private final int    color;
    private final int    direction; // WHITE: -1 (24→1), BLACK: +1 (1→24)

    private int piecesOnBar;
    private int piecesBorneOff;
    private int wins;

    public Player(int playerID, String username, int color) {
        this.playerID  = playerID;
        this.username  = username;
        this.color     = color;
        this.direction = (color == WHITE) ? -1 : 1;
    }

    public void incrementBar()      { piecesOnBar++; } //oyuncunun bardaki taş sayısını bir artırır
    public void decrementBar()      { if (piecesOnBar > 0) piecesOnBar--; } //Oyuncunun bardaki taş sayısını bir azaltır
    public boolean hasBarPiece()    { return piecesOnBar > 0; } //barda taş var mı?
    public void incrementBorneOff() { piecesBorneOff++; } //oyuncunun topladığı taş sayısı
    public boolean hasWon()         { return piecesBorneOff >= 15; } //15 taş topladıysa kazanır
    public void incrementWins()     { wins++; } //galibiyet sayısını artıtır

    public int    getPlayerID()          { return playerID; }
    public String getUsername()          { return username; }
    public int    getColor()             { return color; }
    public int    getDirection()         { return direction; }
    public int    getPiecesOnBar()       { return piecesOnBar; }
    public void   setPiecesOnBar(int v)  { this.piecesOnBar = v; }
    public int    getPiecesBorneOff()    { return piecesBorneOff; }
    public void   setPiecesBorneOff(int v){ this.piecesBorneOff = v; }
    public int    getWins()              { return wins; }
    public void   setWins(int v)         { this.wins = v; }
}