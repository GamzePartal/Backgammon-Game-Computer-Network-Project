package Backgammon.common;

public enum MessageType {
    PLAYER_JOIN,      // Oyuncu sunucuya bağlandı
    GAME_START,       // İki oyuncu hazır oyun başlıyor
    ROLL_DICE,        // Oyuncu zar atmak istiyor
    DICE_RESULT,      // Zar sonucu
    MOVE_PIECE,       // Oyuncu hamle yapıyor
    BOARD_UPDATE,     // Güncel tahta durumu
    TURN_CHANGE,      // Sıra değişti
    HIT_PIECE,        // Taş kırıldı bara gönderildi
    BEAR_OFF,         // Taş toplama
    GAME_OVER,        // Oyun bitti
    ERROR,            // Hata mesajı
    REMATCH_REQUEST,  // Yeniden oynama isteği
    REMATCH_ACCEPT,   // Yeniden oynama kabul edildi
    PLAYER_DISCONNECT,// Oyuncu ayrıldı
    WAITING           // Rakip bekleniyor
}