package Backgammon.common;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int WHITE = 1;
    public static final int BLACK = 2;
    private int playerID;  // Sunucu tarafından atanan benzersiz oyuncu kimliği (1 veya 2)
    private String username;  // Oyuncunun giriş ekranında girdiği kullanıcı adı 
    private int color;  // Oyuncunun taş rengi: WHITE (1) veya BLACK (2)

    // Tavla'da oyun yönü:
    //  WHITE oyuncu: 24'ten 1'e doğru hareket eder (yön = -1)
    // BLACK oyuncu: 1'den 24'e doğru hareket eder (yön = +1)
    private int direction;

    
    private int piecesOnBar; // barda bekleyen taş sayısı rakibin kırdığı taşlar 
    private int piecesBorneOff;  // Oyundan başarıyla çıkarılan taş sayısı

    
    public Player(int playerID, String username, int color) {
        this.playerID = playerID;
        this.username = username;
        this.color = color;
        this.piecesOnBar = 0;
        this.piecesBorneOff = 0;

        // Renk bilgisine göre hareket yönünü otomatik belirle
        // WHITE: sağdan sola (24→1), BLACK: soldan sağa (1→24)
        this.direction = (color == WHITE) ? -1 : 1;
    }

    
    //barda bekleyen taşların artma methodu
    public void incrementBar() {
        this.piecesOnBar++;
    }

    //oyuncnun bardaki taşını tahtaya geri alması durumu
    public void decrementBar() {
        if (this.piecesOnBar > 0) {
            this.piecesOnBar--;
        }
    }

    //Oyuncunun barda taşı olup olmadığını kontrol eder. Barda taş varken başka hamle yapamaz 
    public boolean hasBarPiece() {
        return this.piecesOnBar > 0;
    }


    //oyundan toplanan taşların sayısını arttıran method
    public void incrementBorneOff() {
        this.piecesBorneOff++;
    }

   
    // oyuncu 15 taşı topyadı mı evetse oyunu kazanır
    public boolean hasWon() {
        return this.piecesBorneOff >= 15;
    }

    //1 ya da 2
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

    //Oyuncunun barda kaç taşı olduğunu doğrudan ayarlar. GameState güncelleme işlemlerinde kullanılır.
    public void setPiecesOnBar(int piecesOnBar) {
        this.piecesOnBar = piecesOnBar;
    }

   
    public int getPiecesBorneOff() {
        return piecesBorneOff;
    }

    
    public void setPiecesBorneOff(int piecesBorneOff) {
        this.piecesBorneOff = piecesBorneOff;
    }

    

}
