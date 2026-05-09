package Backgammon.common;

import java.io.Serializable;

public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int WHITE = 1;
    public static final int BLACK = 2;

    private int playerID;    // Sunucu tarafından atanan benzersiz oyuncu kimliği (1 veya 2)
    private String username;    // Oyuncunun giriş ekranında girdiği kullanıcı adı
    private int color;       // Oyuncunun taş rengi: WHITE (1) veya BLACK (2)

    // Tavla'da oyun yönü:
    //  WHITE oyuncu: 24'ten 1'e doğru hareket eder (yön = -1)
    //  BLACK oyuncu: 1'den 24'e doğru hareket eder (yön = +1)
    private int direction;

    private int piecesOnBar;    // Barda bekleyen taş sayısı (rakibin kırdığı taşlar)
    private int piecesBorneOff; // Oyundan başarıyla çıkarılan taş sayısı

    // Kazanılan oyun sayısı — oyun bittikten sonra kazanan oyuncuda 1 artırılır
    private int wins;

    public Player(int playerID, String username, int color) {
        this.playerID = playerID;
        this.username = username;
        this.color = color;
        this.piecesOnBar = 0;
        this.piecesBorneOff = 0;
        this.wins = 0;

        // Renk bilgisine göre hareket yönünü otomatik belirle
        // WHITE: sağdan sola hareket eder (24→1), evi sol altta 1-6 (indeks 0-5),  yön = -1
        // BLACK: soldan sağa hareket eder (1→24), evi sağ üstte 19-24 (indeks 18-23), yön = +1
        this.direction = (color == WHITE) ? -1 : 1;
    }

    public void incrementBar() {
        this.piecesOnBar++;
    }

    public void decrementBar() {
        if (this.piecesOnBar > 0) {
            this.piecesOnBar--;
        }
    }

    public boolean hasBarPiece() {
        return this.piecesOnBar > 0;
    }

    public void incrementBorneOff() {
        this.piecesBorneOff++;
    }

    public boolean hasWon() {
        return this.piecesBorneOff >= 15;
    }

    public void incrementWins() {
        this.wins++;
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getUsername() {
        return username;
    }

    public int getColor() {
        return color;
    }

    public int getDirection() {
        return direction;
    }

    public int getPiecesOnBar() {
        return piecesOnBar;
    }

    public void setPiecesOnBar(int piecesOnBar) {
        this.piecesOnBar = piecesOnBar;
    }

    public int getPiecesBorneOff() {
        return piecesBorneOff;
    }

    public void setPiecesBorneOff(int piecesBorneOff) {
        this.piecesBorneOff = piecesBorneOff;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }
}
