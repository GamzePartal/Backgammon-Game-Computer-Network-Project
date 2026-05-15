package Backgammon.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//zar atma ve kala hamle haklarını yönetir
public class Dice implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Random random = new Random();
    private int die1, die2;
    private final List<Integer> remainingMoves = new ArrayList<>();
    private boolean rolled;

    //İki zar atılır çift gelirse dört hamle hakkı, normal gelirse iki hamle hakkı oluşur
    public int[] roll() {
        die1 = random.nextInt(6) + 1;
        die2 = random.nextInt(6) + 1;
        remainingMoves.clear();
        int repeats = isDoubles() ? 4 : 2;
        for (int i = 0; i < repeats; i++) {
            remainingMoves.add(die1);
        }
        if (!isDoubles()) {
            remainingMoves.set(1, die2);
        }
        rolled = true;
        return new int[]{die1, die2};
    }

    //Kullanılan zar değerini kalan hamle listesinden siler
    public boolean useDie(int value) {
        int idx = remainingMoves.indexOf(value);
        if (idx < 0) {
            return false;
        }
        remainingMoves.remove(idx);
        return true;
    }

    //verilen zar değeri kullanılabilir mi
    public boolean canUse(int value) {
        return remainingMoves.contains(value);
    }

    //tüm hamleler kullanıldı ı
    public boolean allUsed() {
        return remainingMoves.isEmpty();
    }

    //zar atıldı mı
    public boolean isRolled() {
        return rolled;
    }

    //zarlar çift mi geldi
    public boolean isDoubles() {
        return die1 == die2;
    }

    public int getDie1() {
        return die1;
    }

    public int getDie2() {
        return die2;
    }

    public int[] getValues() {
        return new int[]{die1, die2};
    }

    //kalan hamle hakkını döndür
    public int getRemainingCount() {
        return remainingMoves.size();
    }

    //kalan zar hamlelerini list olarak döndür
    public List<Integer> getRemainingMoves() {
        return new ArrayList<>(remainingMoves);
    }

    //zarları ve kalan hamleleri sıfırla yeni sıra
    public void reset() {
        die1 = die2 = 0;
        remainingMoves.clear();
        rolled = false;
    }
}
