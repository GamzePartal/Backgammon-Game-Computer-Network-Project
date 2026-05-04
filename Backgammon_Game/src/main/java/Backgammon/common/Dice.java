package Backgammon.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dice implements Serializable {

    private static final long serialVersionUID = 1L;  // Java serileştirme versiyonu
    private final Random random;  // Zar atmak için kullanılan rastgele sayı üreteci
    private int die1;// zarlar 1-6 arasında değer alır
    private int die2;

  
    // kullanılabilecek hamle hakkını tutan liste
    private List<Integer> remainingMoves;  

    // Zarların atılıp atılmadığını takip eden bayrak
    private boolean rolled;

   
    public Dice() {
        this.random = new Random();
        this.remainingMoves = new ArrayList<>(); 
        this.rolled = false;
        this.die1 = 0;
        this.die2 = 0;
    }

   
    public int[] roll() {
        // 1-6 arası iki rastgele değer üret
        die1 = random.nextInt(6) + 1;
        die2 = random.nextInt(6) + 1;

        // Kalan hamle listesini temizle ve yeni değerleri ekle
        remainingMoves.clear();

        if (isDoubles()) {
            // Çift geldi 4 hamle hakkı oldu
            remainingMoves.add(die1);
            remainingMoves.add(die1);
            remainingMoves.add(die1);
            remainingMoves.add(die1);
        } else {
            // Normal atış 2 hamle hakkı
            remainingMoves.add(die1);
            remainingMoves.add(die2);
        }

        this.rolled = true;
        return new int[]{die1, die2};
    }

  
    
    // verilen zar değerini kullanıldı mı? true ise kullanıldı false ise o değer listede yok
    public boolean useDie(int value) {
        
        for (int i = 0; i < remainingMoves.size(); i++) {
            if (remainingMoves.get(i) == value) {
                remainingMoves.remove(i); 
                return true;
            }
        }
        return false; 
    }

   
    // verilen zar değerinin hala kullanılabilir olup olmadığına bakılır true ise hamle yapıabilir
    public boolean canUse(int value) {
        return remainingMoves.contains(value);
    }

    //Zarları sıfırlar ve yeni tur için hazırlar sıra değiştiğinde çağır
    public void reset() {
        die1 = 0;
        die2 = 0;
        remainingMoves.clear();
        rolled = false;
    }

    
    public boolean isDoubles() {
        return die1 == die2;
    }

   
    // tüm hamleler kullanıldı mı
    public boolean allUsed() {
        return remainingMoves.isEmpty();
    }

    
    // zarlar atıldı mı? true ise atldı
    public boolean isRolled() {
        return rolled;
    }

    
    public int getDie1() {
        return die1;
    }

    
    public int getDie2() {
        return die2;
    }

    
    //Atılan zar değerlerini dizi olarak döner
    public int[] getValues() {
        return new int[]{die1, die2};
    }

    
    //kalan hamle haklarının listesidir liste değiştirilmesin diye kopya döndürüyoz
    public List<Integer> getRemainingMoves() {
        return new ArrayList<>(remainingMoves);
    }

    
    //kalan hamle sayısı
    public int getRemainingCount() {
        return remainingMoves.size();
    }

   
}
