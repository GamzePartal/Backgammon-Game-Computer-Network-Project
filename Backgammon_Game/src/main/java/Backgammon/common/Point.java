package Backgammon.common;
import java.io.Serializable;

public class Point implements Serializable{
  
    private static final long serialVersionUID = 1L;
    private int index; // Bu noktanın tahtadaki indeksi (0-23 arası, 0 = 1. nokta)
    private int count; // Bu noktada kaç taş bulunduğu (0-15 arası olabilir)
 
    // noktadaki taşın sahibi olan oyuncunun rengi  Player.WHITE, Player.BLACK veya 0 (boş nokta) değerini alır
    private int owner;
 
    
    public Point(int index) {
        this.index = index;
        this.count = 0;
        this.owner = 0; // 0 = boş, sahipsiz
    }
 
    //Oyun başlangıç dizilimini kurarken kullanılır
    public Point(int index, int count, int owner) {
        this.index = index;
        this.count = count;
        this.owner = owner;
    }
 
    
  
    //taşı eklenecek olan oyuncunun rengi verilir, oyuncunun taşı eklenir nokta boşsa sahiplik oyuncuya geçer
    public void addPiece(int playerColor) {
        this.count++;
        this.owner = playerColor;
    }
 
 
    // haneden bir taş silinir eğer 1 taş varsa hane boşalır ve sahipsiz olur eğer hiç taş yoksa işlem yapılmaz
    public void removePiece() {
        if (this.count > 0) {
            this.count--;
            // Tüm taşlar gitti, nokta artık sahipsiz
            if (this.count == 0) {
                this.owner = 0;
            }
        }
    }
 
  
    //Noktanın boş olup olmadığını kontrol eder
    public boolean isEmpty() {
        return this.count == 0;
    }
 

    //hanede yalnızca bir taş mı var? tek taş olursa kırılacak
    public boolean isBlot() {
        return this.count == 1;
    }
 
   
    //Belirtilen oyuncunun bu haneye taş koyup koyamayacağına bakar
    public boolean isOpenFor(int playerColor) {
        if (isEmpty()) return true;
        
        if (this.owner == playerColor) return true;  // Kendi taşı olan hane açık
       
        if (this.owner != playerColor && isBlot()) return true;   // Rakibin tek taşı varsa taşı kırabilir
      
        return false;   // Rakibin 2den fazla taşı varsa false dner
    }
 
 
    //hanenin belirtilen oyuncuya kapalı olup olmadığına bakılır
    //Rakibin 2 veya daha fazla taşı varsa nokta o oyuncuya kapalıdır. true ise oyuncu taş koyamaz
    public boolean isBlockedFor(int playerColor) {
        return this.owner != playerColor && this.count >= 2;
    }
 
    
   //hanenin tahtadaki indeksini döner
    public int getIndex() {
        return index;
    }
 
    //hanedeki taş sayısını döner
    public int getCount() {
        return count;
    }
 
   
    //hanedeki taş sayısını ayarlar tahta güncellenirsen vs
    public void setCount(int count) {
        this.count = count;
    }
 
    
    public int getOwner() {
        return owner;
    }
 
    //noktadaki taşların sahibini günceller
    public void setOwner(int owner) {
        this.owner = owner;
    }
 
  
}