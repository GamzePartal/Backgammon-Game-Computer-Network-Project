package Backgammon.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//server tarafındaki log mesajlarını düzenli ve renkli şekilde konsola yazdırır
public class ServerLogger {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";

    
    private static final DateTimeFormatter FMT
            = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    //Güncel tarih ve saati formatlı string olarak döndürür.
    private static String ts() {
        return LocalDateTime.now().format(FMT);
    }

    public static void log(String msg) {
        System.out.println(GREEN + "[INFO]  " + ts() + " " + msg + RESET);
    }

    public static void logError(String msg) {
        System.err.println(RED + "[HATA]  " + ts() + " " + msg + RESET);
    }

    public static void logWarning(String msg) {
        System.out.println(YELLOW + "[UYARI] " + ts() + " " + msg + RESET);
    }

    public static void logGame(String roomId, String event) {
        System.out.println(BLUE + "[OYUN]  " + ts() + " [Oda:" + roomId + "] " + event + RESET);
    }

    //Ağ bağlantısı ile ilgili mesajları loglar
    public static void logNetwork(String msg) {
        System.out.println(CYAN + "[AG]    " + ts() + " " + msg + RESET);
    }

    //Server başlatıldığında port ve tarih bilgisini yazar
    public static void logStartup(int port) {
        System.out.println(GREEN + "\n TAVLA SUNUCUSU BASLATILIYOR\n"
                + " Port  : " + port + "\n Tarih : " + ts()
                + "\n Durum : Baglanti bekleniyor\n" + RESET);
    }

    //Server kapatılırken kapanış mesajı yazar
    public static void logShutdown() {
        System.out.println(RED + "\n[KAPANIS] Sunucu kapatiliyor" + RESET);
    }
}
