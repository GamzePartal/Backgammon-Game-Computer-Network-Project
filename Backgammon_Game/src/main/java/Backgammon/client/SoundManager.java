package Backgammon.client;

import javazoom.jl.player.advanced.AdvancedPlayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;


public class SoundManager {

    private static final String BASE_PATH      = "src/sounds/";
    private static final String BACKGROUND     = BASE_PATH + "backround.mp3";
    private static final String DICE_ROLL      = BASE_PATH + "roll_dice.mp3";
    private static final String PIECE_MOVE     = BASE_PATH + "button_press.mp3";
    private static final String PIECE_HIT      = BASE_PATH + "piece_hit.mp3";
    private static final String WIN            = BASE_PATH + "win.mp3";

 
    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private SoundManager() {}

    // Arkaplan müziği (döngülü) 
    private Thread     bgThread;
    private volatile boolean bgRunning = false;
    private AdvancedPlayer bgPlayer;

   
    public void playBackground() {
        if (bgRunning) return;

        bgRunning = true;
        bgThread  = new Thread(() -> {
            while (bgRunning) {
                try {
                    File f = new File(BACKGROUND);
                    if (!f.exists()) {
                        System.err.println("[SES] Arkaplan dosyası bulunamadı: " + BACKGROUND);
                        bgRunning = false;
                        break;
                    }
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bgPlayer = new AdvancedPlayer(bis);
                    bgPlayer.play(); // Dosya bitene kadar bloklar
                } catch (Exception e) {
                    if (bgRunning) {
                        System.err.println("[SES] Arkaplan çalma hatası: " + e.getMessage());
                    }
                    // bgRunning=false ise durduruldu, döngüden çık
                    break;
                }
            }
        }, "BackgroundMusic");
        bgThread.setDaemon(true);
        bgThread.start();
    }

    // arka plan müziği
    public void stopBackground() {
        bgRunning = false;
        if (bgPlayer != null) {
            bgPlayer.close();
            bgPlayer = null;
        }
        if (bgThread != null) {
            bgThread.interrupt();
            bgThread = null;
        }
    }

   

    // Zar atma sesi 
    public void playDiceRoll() {
        playOnce(DICE_ROLL);
    }

    // Taş hamlesi sesi 
    public void playPieceMove() {
        playOnce(PIECE_MOVE);
    }

    // Taş kırılma (hit) sesi 
    public void playPieceHit() {
        playOnce(PIECE_HIT);
    }

    /// Kazanma sesi 
    public void playWin() {
        playOnce(WIN);
    }

  
    private void playOnce(String path) {
        Thread t = new Thread(() -> {
            try {
                File f = new File(path);
                if (!f.exists()) {
                    System.err.println("[SES] Dosya bulunamadı: " + path);
                    return;
                }
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);
                AdvancedPlayer player = new AdvancedPlayer(bis);
                player.play();
            } catch (Exception e) {
                System.err.println("[SES] Çalma hatası (" + path + "): " + e.getMessage());
            }
        }, "SoundEffect");
        t.setDaemon(true);
        t.start();
    }
}