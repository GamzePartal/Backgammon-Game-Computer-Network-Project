
package Backgammon.common;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 

public class Board implements Serializable{
   
   private static final long serialVersionUID = 1L;   
   public static final int POINT_COUNT = 24;  // tahtadaki toplam hane sayısı 
   public static final int TOTAL_PIECES = 15;  // oyun başında her oyuncunun sahip olduğu toplam taş sayısı
   private Point[] points; // 24 haneyi tutan dizi index 0 = 1. hane 

    
    public Board() {
        points = new Point[POINT_COUNT];
        for (int i = 0; i < POINT_COUNT; i++) {
            points[i] = new Point(i);
        }
        initBoard();
    }
 
   
    //standart tavla taşları dizimi
    private void initBoard() {
        
        // white taşlar (renk = Player.WHITE = 1)
        setPoint(0, 2, Player.WHITE);         
        setPoint(11, 5, Player.WHITE); // 12. hanede 5 beyaz taş (indeks 11)
        setPoint(16, 3, Player.WHITE);       
        setPoint(18, 5, Player.WHITE);
 
        // black taşlar (renk = Player.BLACK = 2)
        setPoint(23, 2, Player.BLACK); // 24. hanede 2 siyah taş (indeks 23)
        setPoint(12, 5, Player.BLACK);
        setPoint(7, 3, Player.BLACK);
        setPoint(5, 5, Player.BLACK);
    }
 
    //yalnızca initboard veya tahta sıfırlama için kullanılır
    private void setPoint(int index, int count, int owner) {
        points[index].setCount(count);
        points[index].setOwner(owner);
    }
 

    //oyuncu verilen zar değerleriyle yapabileceği tüm geçerli  hamleleri hesaplar ve döner. Barda taş varsa yalnızca bar hamlelerini döner.
     public List<int[]> getAvailableMoves(Dice dice, Player player) {
        List<int[]> moves = new ArrayList<>();
        int color = player.getColor();
 
        for (int dieVal : dice.getRemainingMoves()) {
 
            if (player.hasBarPiece()) { //barda taş varsa önce onu kullan
                int target = getBarEntryPoint(dieVal, player);
                if (target >= 0 && target < POINT_COUNT && points[target].isOpenFor(color)) {
                    moves.add(new int[]{-1, target}); // -1 = bar
                }
            } else {
                for (int i = 0; i < POINT_COUNT; i++) {
                    if (points[i].getOwner() != color || points[i].isEmpty()) continue;
 
                    int target = i + (dieVal * player.getDirection());
 
                    if (target >= 0 && target < POINT_COUNT && points[target].isOpenFor(color)) {
                        moves.add(new int[]{i, target});
                    } else if (canBearOff(player)) {
                        moves.add(new int[]{i, -2}); // -2 = bearing off
                    }
                }
            }
        }
 
        return moves;
    }
 
  
 
     //Oyuncunun bardan gireceği hedef noktanın indeksini hesaplar
    private int getBarEntryPoint(int dieVal, Player player) {
        if (player.getColor() == Player.WHITE) {       
            return POINT_COUNT - dieVal; // white karşı taraftan (24. noktadan) giriş yapar
        } else {
            return dieVal - 1;  // black kendi tarafından (1. noktadan) giriş yapar
        }
    }
 
 
    //Belirli bir hamlenin from -> to geçerli olup olmadığına bakar Bar hamlesi için from = -1 kullanılır.
    public boolean isValidMove(int from, int to, Dice dice, Player player) {
        if (!dice.isRolled()) return false;
 
        if (to == -2) { 
            return canBearOff(player);
        }
 
        if (to < 0 || to >= POINT_COUNT) return false;  // hedef tahta sınırları içinde olmalı
 
        if (!points[to].isOpenFor(player.getColor())) return false; // hedef nokta, oyuncuya açık olmalı
 
        if (from == -1) {
            if (!player.hasBarPiece()) return false; 
            int dieVal = (player.getColor() == Player.WHITE)
                    ? (POINT_COUNT - to)
                    : (to + 1);
            return dice.canUse(dieVal);
        } else {
            if (from < 0 || from >= POINT_COUNT) return false;
            if (points[from].getOwner() != player.getColor()) return false;
            if (points[from].isEmpty()) return false;
 
            // kullanılacak zar değeri mesafeye eşit olmalı
            int distance = Math.abs(to - from);
            return dice.canUse(distance);
        }
    }
 
   

    //Belirtilen hamlede taşı kaynaktan hedefe taşır , bar hamlesi için from = -1 kullanılır.
    public void movePiece(int from, int to, Player movingPlayer, Player opponent, Dice dice) {
        int playerColor = movingPlayer.getColor();
 
        // hedefte rakibin tek taşı varsa kırılır ve bara gönderilir
        if (!points[to].isEmpty() && points[to].getOwner() != playerColor && points[to].isBlot()) {
            hitPiece(to, opponent);
        }
 
        // kaynaktan taş kaldır
        if (from == -1) {
            movingPlayer.decrementBar();
            int dieVal = (playerColor == Player.WHITE) ? (POINT_COUNT - to): (to + 1);
            dice.useDie(dieVal);
        } else {
            points[from].removePiece();
            int distance = Math.abs(to - from);
            dice.useDie(distance);
        }
 
        // Hedefe taş koy
        points[to].addPiece(playerColor);
    }
 
   
    //rakip taş kırılır 
    public void hitPiece(int pointIndex, Player opponent) { 
        points[pointIndex].removePiece(); // Noktadaki taşı kaldır 
        opponent.incrementBar();   // Rakibin bar sayısını artır
    }
 
    
    //hanedeki taşlar toplanır tüm taşları toplandığında oyunu kazanır
    public void bearOff(int from, Player player, int dieVal, Dice dice) {
        points[from].removePiece();    
        player.incrementBorneOff();  
        dice.useDie(dieVal); // Kullanılan zar değerini işaretle
    }
 

    // oyuncu tüm taşları kendi bölgesine taşıdı mı toplamak için 
    // white ın bölgesi noktalar 0-5 (1-6 arası), blackin bölgesi noktalar 18-23 (19-24 arası)
    public boolean canBearOff(Player player) {
        int color = player.getColor();
   
        if (player.hasBarPiece()) return false; // Barda taş varsa toplamayapılamaz
 
        if (color == Player.WHITE) {
            for (int i = 6; i < POINT_COUNT; i++) {
                if (points[i].getOwner() == Player.WHITE && points[i].getCount() > 0) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < 18; i++) {
                if (points[i].getOwner() == Player.BLACK && points[i].getCount() > 0) {
                    return false;
                }
            }
        }
 
        return true;
    }
 

  
    // oyuncu oyunu kazandı mı? 15 taş toplayan kazanır
    public boolean checkWinner(Player player) {
        return player.hasWon();
    }
 

    //verilen indeksteki Point nesnesini döner
    public Point getPoint(int index) {
        if (index < 0 || index >= POINT_COUNT) return null;
        return points[index];
    }
 
    
    //tüm noktaları yani haneleri döner 24 elemanlı
    public Point[] getPoints() {
        return points;
    }
 
   
    //tahtanın durumunu sıfırlar
    public void reset() {
        for (int i = 0; i < POINT_COUNT; i++) {
            points[i] = new Point(i);
        }
        initBoard();
    }
 
   
    //Tahta nesnesinin derin kopyasını oluştur
    //hamle simülasyonu veya geçerli hamle hesaplamasında değişiklik gerçek tahtaya yansımasın diye kullanılır.
    public Board cloneBoard() {
        Board clone = new Board();
        for (int i = 0; i < POINT_COUNT; i++) {
            clone.points[i].setCount(this.points[i].getCount());
            clone.points[i].setOwner(this.points[i].getOwner());
        }
        return clone;
    }
 

    
}
