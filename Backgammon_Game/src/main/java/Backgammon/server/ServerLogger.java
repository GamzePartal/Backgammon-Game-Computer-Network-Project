package Backgammon.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Sunucu tarafındaki konsol log işlemlerini yöneten yardımcı sınıf
public class ServerLogger {

    // Konsol renk kodları (ANSI escape kodları - Linux/Mac terminallerde desteklenir)
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";

    // Zaman damgası formatı: [GG.AA.YYYY SS:DD:SS]
    private static final DateTimeFormatter FORMATTER
            = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    // genel bilgi mesajlarını yeşil renkte konsola yazar bağlantı başlangıc vs
    public static void log(String message) {
        System.out.println(GREEN + "[INFO]  " + getTimestamp() + " " + message + RESET);
    }

    //hata mesjalarını yazdırır bağlantı kopması geçersiz mesaj vs
    public static void logError(String message) {
        System.err.println(RED + "[HATA]  " + getTimestamp() + " " + message + RESET);
    }

    //uyarı mesajını konsola yazdırır 
    public static void logWarning(String message) {
        System.out.println(YELLOW + "[UYARI] " + getTimestamp() + " " + message + RESET);
    }

    //oyun odasına ait değişikleri yazdırır tur değişimi, hamle, zar gibi
    public static void logGame(String roomId, String event) {
        System.out.println(BLUE + "[OYUN]  " + getTimestamp() + " [Oda:" + roomId + "] " + event + RESET);
    }

   
    // ağ bağlantısıyla ilgili olayları yazdırır client bağlantısı,kopması vs 
    public static void logNetwork(String message) {
        System.out.println(CYAN + "[AĞ]    " + getTimestamp() + " " + message + RESET);
    }

    
    // sunucunun başarıyla başladığını kullancııya gösterir
    public static void logStartup(int port) {
        System.out.println(GREEN);
        System.out.println(" TAVLA SUNUCUSU BAŞLATILIYOR ");
        System.out.println("");
        System.out.println(" Port    : " + port + " ");
        System.out.println(" Tarih   : " + getTimestamp());
        System.out.println(" Durum   : Bağlantı bekleniyor");
        System.out.println("");
        System.out.println(RESET);
    }

    
    //sunucu kapatıldığında konsola yazdırı
    public static void logShutdown() {
        System.out.println(RED + "\n[KAPANIŞ] Sunucu kapatılıyor" + RESET);
    }

    
    //tarih saat string olarak döndürür
    private static String getTimestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }

}