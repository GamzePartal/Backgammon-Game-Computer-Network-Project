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
 
   
    // Standart tavla başlangıç dizilimi
    // WHITE: sağdan sola hareket eder, ev bölgesi 1-6 (indeks 0-5, sol alt)
    // BLACK: soldan sağa hareket eder, ev bölgesi 19-24 (indeks 18-23, sağ üst)
    private void initBoard() {
        // WHITE taşlar (sağdan sola gider, evi solda 0-5)
        setPoint(23, 2, Player.WHITE);  // hane 24: 2 beyaz
        setPoint(12, 5, Player.WHITE);  // hane 13: 5 beyaz
        setPoint(7,  3, Player.WHITE);  // hane  8: 3 beyaz
        setPoint(5,  5, Player.WHITE);  // hane  6: 5 beyaz

        // BLACK taşlar (soldan sağa gider, evi sağda 18-23)
        setPoint(0,  2, Player.BLACK);  // hane  1: 2 siyah
        setPoint(11, 5, Player.BLACK);  // hane 12: 5 siyah
        setPoint(16, 3, Player.BLACK);  // hane 17: 3 siyah
        setPoint(18, 5, Player.BLACK);  // hane 19: 5 siyah
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
        boolean bearingOff = canBearOff(player);

        for (int dieVal : dice.getRemainingMoves()) {

            if (player.hasBarPiece()) { // barda taş varsa önce onu kullan
                int target = getBarEntryPoint(dieVal, player);
                if (target >= 0 && target < POINT_COUNT && points[target].isOpenFor(color)) {
                    // duplicate kontrolü
                    boolean found = false;
                    for (int[] m : moves) {
                        if (m[0] == -1 && m[1] == target) { found = true; break; }
                    }
                    if (!found) moves.add(new int[]{-1, target});
                }
            } else {
                for (int i = 0; i < POINT_COUNT; i++) {
                    if (points[i].getOwner() != color || points[i].isEmpty()) continue;

                    int target = i + (dieVal * player.getDirection());

                    if (target >= 0 && target < POINT_COUNT && points[target].isOpenFor(color)) {
                        // duplicate kontrolü
                        boolean found = false;
                        for (int[] m : moves) {
                            if (m[0] == i && m[1] == target) { found = true; break; }
                        }
                        if (!found) moves.add(new int[]{i, target});
                    } else if (bearingOff) {
                        // Taş toplama: zar değeri tam eşleşmeli ya da en uzak taşta büyük zar olabilir
                        if (canBearOffFrom(i, dieVal, player)) {
                            boolean found = false;
                            for (int[] m : moves) {
                                if (m[0] == i && m[1] == -2) { found = true; break; }
                            }
                            if (!found) moves.add(new int[]{i, -2});
                        }
                    }
                }
            }
        }

        return moves;
    }

    
    private boolean canBearOffFrom(int from, int dieVal, Player player) {
        int color = player.getColor();
        if (color == Player.WHITE) {
            // WHITE ev: 0-5, çıkış sola (hareket yönü -1). 
            // from=0 → dist=1, from=5 → dist=6
            int distToExit = from + 1;
            if (dieVal == distToExit) return true;
            if (dieVal > distToExit) {
                // Zar büyükse: daha geride (Beyaz için daha YÜKSEK indekslerde) taş olmamalıdır.
                // Çünkü beyazlar 24'ten 1'e (indeks 23'ten 0'a) doğru ilerler.
                for (int i = from + 1; i <= 5; i++) {
                    if (points[i].getOwner() == color && points[i].getCount() > 0) return false;
                }
                return true;
            }
        } else {
            // BLACK ev: 18-23, çıkış sağa (hareket yönü +1). 
            // from=23 → dist=1, from=18 → dist=6
            int distToExit = POINT_COUNT - from; // 24 - from
            if (dieVal == distToExit) return true;
            if (dieVal > distToExit) {
                // Zar büyükse: daha geride (Siyah için daha DÜŞÜK indekslerde) taş olmamalıdır.
                // Çünkü siyahlar 1'den 24'e (indeks 0'dan 23'e) doğru ilerler.
                for (int i = from - 1; i >= 18; i--) {
                    if (points[i].getOwner() == color && points[i].getCount() > 0) return false;
                }
                return true;
            }
        }
        return false;
    }
 
  
 
    // Oyuncunun bardan gireceği hedef noktanın indeksini hesaplar
    // WHITE sağdan girer (rakibin evi sağda = indeks 18-23): zar=1 → indeks 23
    // BLACK soldan girer (rakibin evi solda = indeks 0-5):   zar=1 → indeks 0
    private int getBarEntryPoint(int dieVal, Player player) {
        if (player.getColor() == Player.WHITE) {
            return POINT_COUNT - dieVal;  // zar=1 → indeks 23, zar=6 → indeks 18
        } else {
            return dieVal - 1;            // zar=1 → indeks 0,  zar=6 → indeks 5
        }
    }
 
 
    //Belirli bir hamlenin from -> to geçerli olup olmadığına bakar Bar hamlesi için from = -1 kullanılır.
    public boolean isValidMove(int from, int to, Dice dice, Player player) {
        if (!dice.isRolled()) return false;

        // Barda taş varsa SADECE bar hamlesi yapılabilir
        if (player.hasBarPiece() && from != -1) return false;

        if (to == -2) {
            if (!canBearOff(player)) return false;
            if (from < 0 || from >= POINT_COUNT) return false;
            if (points[from].getOwner() != player.getColor() || points[from].isEmpty()) return false;
            for (int dieVal : dice.getRemainingMoves()) {
                if (canBearOffFrom(from, dieVal, player)) return true;
            }
            return false;
        }

        if (to < 0 || to >= POINT_COUNT) return false;

        if (!points[to].isOpenFor(player.getColor())) return false;

        if (from == -1) {
            if (!player.hasBarPiece()) return false;
            // WHITE sağdan girer: zar = POINT_COUNT - to;  BLACK soldan girer: zar = to + 1
            int dieVal = (player.getColor() == Player.WHITE)
                    ? (POINT_COUNT - to)
                    : (to + 1);
            return dice.canUse(dieVal);
        } else {
            if (from < 0 || from >= POINT_COUNT) return false;
            if (points[from].getOwner() != player.getColor()) return false;
            if (points[from].isEmpty()) return false;

            // BUG FIX: Oyuncunun hareket yönünün tersine oynamadığından emin ol
            // Beyaz (-1) ise to < from olmalı. Siyah (+1) ise to > from olmalı.
            int direction = player.getDirection();
            if ((to - from) * direction <= 0) {
                return false; // Ters yöne (veya olduğu yere) hamle yapılamaz
            }

            int distance = Math.abs(to - from);
            return dice.canUse(distance);
        }
    }
 
   

    //Belirtilen hamlede taşı kaynaktan hedefe taşır , bar hamlesi için from = -1 kullanılır.
    public void movePiece(int from, int to, Player movingPlayer, Player opponent, Dice dice) {
        int playerColor = movingPlayer.getColor();

        if (to == -2) {
            // Taş toplama hamlesi: uygun zar değerini bul ve kullan
            // BUG FIX: Büyük zarı haksız yere harcamamak için önce tam mesafeye uyan zar (exactMatch) aranır.
            int exactMatch = -1;
            int largerMatch = -1;
            int distToExit = (playerColor == Player.WHITE) ? (from + 1) : (POINT_COUNT - from);

            for (int d : dice.getRemainingMoves()) {
                if (canBearOffFrom(from, d, movingPlayer)) {
                    if (d == distToExit) {
                        exactMatch = d;
                        break; // Tam eşleşme bulunduğunda hemen çık, en ideal zar budur.
                    } else if (largerMatch == -1) {
                        largerMatch = d; // Tam eşleşme yoksa kullanılacak yedek (daha büyük) zar.
                    }
                }
            }
            
            int dieVal = (exactMatch != -1) ? exactMatch : largerMatch;
            
            points[from].removePiece();
            movingPlayer.incrementBorneOff();
            if (dieVal != -1) dice.useDie(dieVal);
            return;
        }
 
        // hedefte rakibin tek taşı varsa kırılır ve bara gönderilir
        if (!points[to].isEmpty() && points[to].getOwner() != playerColor && points[to].isBlot()) {
            hitPiece(to, opponent);
        }
 
        // kaynaktan taş kaldır
        if (from == -1) {
            movingPlayer.decrementBar();
            // WHITE sağdan girer: zar = POINT_COUNT - to; BLACK soldan girer: zar = to + 1
            int dieVal = (playerColor == Player.WHITE) ? (POINT_COUNT - to) : (to + 1);
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
 

    // WHITE ev bölgesi: indeks 0-5  (hane 1-6,   sol alt)
    // BLACK ev bölgesi: indeks 18-23 (hane 19-24, sağ üst)
    public boolean canBearOff(Player player) {
        int color = player.getColor();

        if (player.hasBarPiece()) return false;

        if (color == Player.WHITE) {
            // indeks 6-23 arasında beyaz taş varsa henüz toplayamaz
            for (int i = 6; i < POINT_COUNT; i++) {
                if (points[i].getOwner() == Player.WHITE && points[i].getCount() > 0) return false;
            }
        } else {
            // indeks 0-17 arasında siyah taş varsa henüz toplayamaz
            for (int i = 0; i < 18; i++) {
                if (points[i].getOwner() == Player.BLACK && points[i].getCount() > 0) return false;
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