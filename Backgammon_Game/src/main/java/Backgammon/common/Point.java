package Backgammon.common;

import java.io.Serializable;

public class Point implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int index;
    private int count;
    private int owner; // 0 = boş, Player.WHITE veya Player.BLACK

    public Point(int index) {
        this.index = index;
    } //verilen indexte boş hane oluşurur

    //verilen parametrelere göre haneyi oluşturur
    public Point(int index, int count, int owner) {
        this.index = index;
        this.count = count;
        this.owner = owner;
    }

    //haneye taş ekler ve sahibini belirler
    public void addPiece(int playerColor) {
        count++;
        owner = playerColor;
    }

    //haneden taş siler taş kamazsa haneyi boş yapar
    public void removePiece() {
        if (count > 0 && --count == 0) {
            owner = 0;
        }
    }

    public boolean isEmpty() {
        return count == 0;
    }

    //hanede tek taş mı var? varsa kırılacak
    public boolean isBlot() {
        return count == 1;
    }

    //oyuncu için hane oynanabilir duurmda mı?
    public boolean isOpenFor(int playerColor) {
        return isEmpty() || owner == playerColor || isBlot();
    }

    //verilen oyuncu için hane kapalı mı?
    public boolean isBlockedFor(int playerColor) {
        return owner != playerColor && count >= 2;
    }

    public int getIndex() {
        return index;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int c) {
        this.count = c;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int o) {
        this.owner = o;
    }
}
