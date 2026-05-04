
package Backgammon.common;


public enum MessageType {
    
    PLAYER_JOIN, //oyuncu sunucuya bağlandı
    GAME_START, //iki oyuncu hazırsa gönder
    ROLL_DICE, // oyuncu zar atmak istediğinde
    DICE_RESULT, 
    MOVE_PIECE, //oyuncu hamle yapmak istediğinde
    BOARD_UPDATE, //sunucunun güncel tahtayı clientlara göndermesşi
    TURN_CHANGE,
    HIT_PIECE, //taş kırıldı bara gönderildi
    BEAR_OFF, // oyuncu taşını topluyor
    GAME_OVER,
    CHAT,
    ERROR,
    REMATCH_REQUEST, //yeni oyun
    REMATCH_ACCEPT, //karşı taraf yeni oyun istediğin kabul edince 
    PLAYER_DISCONNECT, //oyuncu oyundan ayrıldı
    WAITING //oyuncu sıra bekliyor 
    
}
