package Backgammon.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//tavla tahtasını ve hamle kurallarını yönetir
//Geçerli hamle hesaplama, taş hareketi, taş kırma ve taş toplama işlemlerinin büyük kısmı burada yapılır
public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int POINT_COUNT = 24;
    public static final int TOTAL_PIECES = 15;

    private Point[] points;

   //boardı oluştur
    public Board() {
        points = new Point[POINT_COUNT];
        for (int i = 0; i < POINT_COUNT; i++) {
            points[i] = new Point(i);
        }
        initBoard();
    }

    //beyaz siyah taşları tavla kuralıa göre yerleştir
    private void initBoard() {
        setPoint(23, 2, Player.WHITE);
        setPoint(12, 5, Player.WHITE);
        setPoint(7, 3, Player.WHITE);
        setPoint(5, 5, Player.WHITE);
        setPoint(0, 2, Player.BLACK);
        setPoint(11, 5, Player.BLACK);
        setPoint(16, 3, Player.BLACK);
        setPoint(18, 5, Player.BLACK);
    }

    //belirtilen haneye taş sayısı ve sahip bilgisi ata
    private void setPoint(int index, int count, int owner) {
        points[index].setCount(count);
        points[index].setOwner(owner);
    }

    //oyununun zarlarına göre hamlelerini hesapla
    public List<int[]> getAvailableMoves(Dice dice, Player player) {
        List<int[]> moves = new ArrayList<>();
        int color = player.getColor();
        boolean bearingOff = canBearOff(player);

        for (int dieVal : dice.getRemainingMoves()) {
            if (player.hasBarPiece()) {
                int target = getBarEntryPoint(dieVal, player);
                if (target >= 0 && target < POINT_COUNT && points[target].isOpenFor(color)
                        && noDuplicate(moves, -1, target)) {
                    moves.add(new int[]{-1, target});
                }
            } else {
                for (int i = 0; i < POINT_COUNT; i++) {
                    if (points[i].getOwner() != color || points[i].isEmpty()) {
                        continue;
                    }
                    int target = i + dieVal * player.getDirection();
                    if (target >= 0 && target < POINT_COUNT && points[target].isOpenFor(color)
                            && noDuplicate(moves, i, target)) {
                        moves.add(new int[]{i, target});
                    } else if (bearingOff && canBearOffFrom(i, dieVal, player)
                            && noDuplicate(moves, i, -2)) {
                        moves.add(new int[]{i, -2});
                    }
                }
            }
        }
        return moves;
    }

    //aynı hamlenin listeye tekrar eklenmesini engeller
    private boolean noDuplicate(List<int[]> moves, int from, int to) {
        for (int[] m : moves) {
            if (m[0] == from && m[1] == to) {
                return false;
            }
        }
        return true;
    }

    private boolean canBearOffFrom(int from, int dieVal, Player player) {
        int color = player.getColor();
        if (color == Player.WHITE) {
            int dist = from + 1;
            if (dieVal == dist) {
                return true;
            }
            if (dieVal > dist) {
                for (int i = from + 1; i <= 5; i++) {
                    if (points[i].getOwner() == color && points[i].getCount() > 0) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            int dist = POINT_COUNT - from;
            if (dieVal == dist) {
                return true;
            }
            if (dieVal > dist) {
                for (int i = from - 1; i >= 18; i--) {
                    if (points[i].getOwner() == color && points[i].getCount() > 0) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    //bardaki taşın zar değerine göre hangi haneye gideceğini hesapla
    private int getBarEntryPoint(int dieVal, Player player) {
        return (player.getColor() == Player.WHITE) ? POINT_COUNT - dieVal : dieVal - 1;
    }

    //hamle tavla kurallarına uygun mu
    public boolean isValidMove(int from, int to, Dice dice, Player player) {
        if (!dice.isRolled()) {
            return false;
        }
        if (player.hasBarPiece() && from != -1) {
            return false;
        }

        if (to == -2) {
            if (!canBearOff(player) || from < 0 || from >= POINT_COUNT) {
                return false;
            }
            if (points[from].getOwner() != player.getColor() || points[from].isEmpty()) {
                return false;
            }
            for (int dieVal : dice.getRemainingMoves()) {
                if (canBearOffFrom(from, dieVal, player)) {
                    return true;
                }
            }
            return false;
        }

        if (to < 0 || to >= POINT_COUNT || !points[to].isOpenFor(player.getColor())) {
            return false;
        }

        if (from == -1) {
            if (!player.hasBarPiece()) {
                return false;
            }
            int dieVal = (player.getColor() == Player.WHITE) ? (POINT_COUNT - to) : (to + 1);
            return dice.canUse(dieVal);
        }

        if (from < 0 || from >= POINT_COUNT) {
            return false;
        }
        if (points[from].getOwner() != player.getColor() || points[from].isEmpty()) {
            return false;
        }
        if ((to - from) * player.getDirection() <= 0) {
            return false;
        }
        return dice.canUse(Math.abs(to - from));
    }

    //geçerli hamle tahtaya uygulanır, taşı hareket ettirir, rakip taşı kırabilir veya taş toplar
    public void movePiece(int from, int to, Player movingPlayer, Player opponent, Dice dice) {
        int color = movingPlayer.getColor();

        if (to == -2) {
            int distToExit = (color == Player.WHITE) ? (from + 1) : (POINT_COUNT - from);
            int exactMatch = -1, largerMatch = -1;
            for (int d : dice.getRemainingMoves()) {
                if (!canBearOffFrom(from, d, movingPlayer)) {
                    continue;
                }
                if (d == distToExit) {
                    exactMatch = d;
                    break;
                }
                if (largerMatch == -1) {
                    largerMatch = d;
                }
            }
            int dieVal = (exactMatch != -1) ? exactMatch : largerMatch;
            points[from].removePiece();
            movingPlayer.incrementBorneOff();
            if (dieVal != -1) {
                dice.useDie(dieVal);
            }
            return;
        }

        if (!points[to].isEmpty() && points[to].getOwner() != color && points[to].isBlot()) {
            points[to].removePiece();
            opponent.incrementBar();
        }

        if (from == -1) {
            movingPlayer.decrementBar();
            dice.useDie((color == Player.WHITE) ? POINT_COUNT - to : to + 1);
        } else {
            points[from].removePiece();
            dice.useDie(Math.abs(to - from));
        }

        points[to].addPiece(color);
    }

    //oyuncu taş toplamaya başlayabilir mi
    public boolean canBearOff(Player player) {
        if (player.hasBarPiece()) {
            return false;
        }
        int color = player.getColor();
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

    //oyuncu oyunu kazandı mı
    public boolean checkWinner(Player player) {
        return player.hasWon();
    }

    //tahtadaki belirtilen indexteki haneyi  döndür
    public Point getPoint(int index) {
        return (index < 0 || index >= POINT_COUNT) ? null : points[index];
    }

    public Point[] getPoints() {
        return points;
    }

    //tahtayı temizler ve başlangıç dizilimine getirir
    public void reset() {
        for (int i = 0; i < POINT_COUNT; i++) {
            points[i] = new Point(i);
        }
        initBoard();
    }

    //tahtanın kopyasını oluştur
    public Board cloneBoard() {
        Board clone = new Board();
        for (int i = 0; i < POINT_COUNT; i++) {
            clone.points[i].setCount(points[i].getCount());
            clone.points[i].setOwner(points[i].getOwner());
        }
        return clone;
    }
}
