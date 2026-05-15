package Backgammon.client;

import javazoom.jl.player.advanced.AdvancedPlayer;
import java.io.BufferedInputStream;
import java.io.InputStream;

// oyundaki müzik ve ses efektlerini yönetir
//Singleton yapısı kullanıldığı için uygulama boyunca tek bir SoundManager nesnesi çalışır
public class SoundManager {

    private static final String BACKGROUND = "/sounds/backround.mp3";
    private static final String DICE_ROLL = "/sounds/roll_dice.mp3";
    private static final String PIECE_MOVE = "/sounds/button_press.mp3";
    private static final String PIECE_HIT = "/sounds/piece_hit.mp3";
    private static final String WIN = "/sounds/win.mp3";

    private static volatile SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null)
            synchronized (SoundManager.class) {
            if (instance == null) {
                instance = new SoundManager();
            }
        }
        return instance;
    }

    private SoundManager() {
    }

    private Thread bgThread;
    private volatile boolean bgRunning;
    private final Object bgLock = new Object();
    private AdvancedPlayer bgPlayer;

    public void playBackground() {
        if (bgRunning) {
            return;
        }
        bgRunning = true;
        bgThread = new Thread(() -> {
            while (bgRunning) {
                try {
                    InputStream is = SoundManager.class.getResourceAsStream(BACKGROUND);
                    if (is == null) {
                        bgRunning = false;
                        break;
                    }
                    AdvancedPlayer p = new AdvancedPlayer(new BufferedInputStream(is));
                    synchronized (bgLock) {
                        bgPlayer = p;
                    }
                    p.play();
                } catch (Exception e) {
                    if (bgRunning) {
                        System.err.println("[SES] Arkaplan hatasi: " + e.getMessage());
                    }
                    break;
                }
            }
        }, "BackgroundMusic");
        bgThread.setDaemon(true);
        bgThread.start();
    }

    public void stopBackground() {
        bgRunning = false;
        synchronized (bgLock) {
            if (bgPlayer != null) {
                bgPlayer.close();
                bgPlayer = null;
            }
        }
        if (bgThread != null) {
            bgThread.interrupt();
            bgThread = null;
        }
    }

    public void playDiceRoll() {
        playOnce(DICE_ROLL);
    }

    public void playPieceMove() {
        playOnce(PIECE_MOVE);
    }

    public void playButtonClick() {
        playOnce(PIECE_MOVE);
    }

    public void playPieceHit() {
        playOnce(PIECE_HIT);
    }

    public void playWin() {
        playOnce(WIN);
    }

    //verilen ses dosyasını tek seferlik olarak ayrı threadde çalar
    private void playOnce(String path) {
        Thread t = new Thread(() -> {
            try {
                InputStream is = SoundManager.class.getResourceAsStream(path);
                if (is == null) {
                    return;
                }
                new AdvancedPlayer(new BufferedInputStream(is)).play();
            } catch (Exception e) {
                System.err.println("[SES] Calma hatasi (" + path + "): " + e.getMessage());
            }
        }, "SoundEffect");
        t.setDaemon(true);
        t.start();
    }
}
