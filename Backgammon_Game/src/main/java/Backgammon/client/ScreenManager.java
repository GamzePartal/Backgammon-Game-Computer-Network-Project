package Backgammon.client;

import Backgammon.client.screens.*;
import Backgammon.common.GameMessage;
import Backgammon.common.GameState;
import javax.swing.*;
import java.awt.*;

//Bu class, client tarafındaki ekran geçişlerini ve server’dan gelen mesajların ilgili ekrana aktarılmasını yönetir.
public class ScreenManager {

    private static final String SCREEN_START = "START";
    private static final String SCREEN_GAME = "GAME";
    private static final String SCREEN_END = "END";

    private final JFrame mainFrame;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final StartScreenPanel startScreen;
    private final GameScreenPanel gameScreen;
    private final EndScreenPanel endScreen;

    private BackgammonClient client;
    private String currentScreen = SCREEN_START;

    public ScreenManager(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        startScreen = new StartScreenPanel(this);
        gameScreen = new GameScreenPanel(this);
        endScreen = new EndScreenPanel(this);
        mainPanel.add(startScreen, SCREEN_START);
        mainPanel.add(gameScreen, SCREEN_GAME);
        mainPanel.add(endScreen, SCREEN_END);
        cardLayout.show(mainPanel, SCREEN_START);
    }

    public void showStartScreen() {
        SoundManager.getInstance().playBackground();
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        startScreen.reset();
        show(SCREEN_START);
    }

    public void showGameScreen() {
        show(SCREEN_GAME);
    }

    //Oyun bitince endscreen  gösterir
    public void showEndScreen(String winnerName, boolean isLocalWinner, boolean isMars, int winnerWins) {
        endScreen.setResult(winnerName, isLocalWinner, isMars, winnerWins);
        show(SCREEN_END);
    }

    //verilen ekranı cardlayout ile aktif hale getirir
    private void show(String screen) {
        currentScreen = screen;
        cardLayout.show(mainPanel, screen);
    }

    //client bağlıysa servera tekrar oynama isteği gönderir
    public void requestRematch() {
        if (client != null && client.isConnected()) {
            client.sendRematchRequest();
            endScreen.showWaitingForRematch();
        }
    }

    //serverdan gelen messaj işlenir
    public void onMessageReceived(GameMessage message) {
        SwingUtilities.invokeLater(() -> processMessage(message));
    }

    //Mesaj türüne göre ekran güncellemesi yapar
    //Oyun başlatma, tahta güncelleme, hata gösterme ve oyun bitiş işlemlerini yönetir.
    private void processMessage(GameMessage message) {
        switch (message.getType()) {
            case GAME_START:
                if (message.getData() instanceof GameState) {
                    gameScreen.initGame((GameState) message.getData(), client);
                    showGameScreen();
                }
                break;
            case BOARD_UPDATE:
                if (message.getData() instanceof GameState) {
                    gameScreen.updateBoard((GameState) message.getData());
                }
                break;
            case DICE_RESULT:
                gameScreen.onDiceResult(message);
                break;
            case TURN_CHANGE:
                gameScreen.onTurnChange(message);
                break;
            case GAME_OVER:
                handleGameOver(message);
                break;
            case ERROR:
                String err = message.getData() instanceof String ? (String) message.getData() : "Hata";
                JOptionPane.showMessageDialog(mainFrame, err, "Hata", JOptionPane.ERROR_MESSAGE);
                break;
            case PLAYER_DISCONNECT:
                SoundManager.getInstance().stopBackground();
                JOptionPane.showMessageDialog(mainFrame,
                        "Rakibiniz oyundan ayrildi. Ana menuye donuluyor...",
                        "Baglanti Kesildi", JOptionPane.WARNING_MESSAGE);
                showStartScreen();
                break;
            case WAITING:
                if (SCREEN_END.equals(currentScreen)) {
                    endScreen.showWaitingForOpponent();
                }
                break;
            default:
                System.out.println("[EKRAN] Islenemeyen mesaj: " + message.getType());
        }
    }

    //Oyun bittiğinde kazananı belirler, sesleri ayarlar ve bitiş ekranını açar
    private void handleGameOver(GameMessage message) {
        if (!(message.getData() instanceof GameState)) {
            return;
        }
        GameState state = (GameState) message.getData();
        String winnerName = state.getWinner() != null ? state.getWinner().getUsername() : "?";
        boolean isLocalWinner = state.getWinner() != null && state.getWinner().getPlayerID() == client.getPlayerID();
        int winnerWins = state.getWinner() != null ? state.getWinner().getWins() : 0;

        SoundManager.getInstance().stopBackground();
        if (isLocalWinner) {
            SoundManager.getInstance().playWin();
        }
        showEndScreen(winnerName, isLocalWinner, state.isMars(), winnerWins);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            SoundManager.getInstance().playBackground();
        }, "BgRestart").start();
    }

    //Server bağlantısı koparsa kullanıcıya uyarı gösterir ve ana menüye döner
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            SoundManager.getInstance().stopBackground();
            JOptionPane.showMessageDialog(mainFrame, "Sunucu baglantisi kesildi!",
                    "Baglanti Hatasi", JOptionPane.ERROR_MESSAGE);
            showStartScreen();
        });
    }

    //Yeni bir BackgammonClient oluşturur ve servera bağlanmayı dener
    //başarılı olursa kullanıcı adını servera gönderir
    public boolean connectToServer(String ip, int port, String username) {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        client = new BackgammonClient(this);
        if (client.connect(ip, port)) {
            client.sendPlayerJoin(username);
            return true;
        }
        return false;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public BackgammonClient getClient() {
        return client;
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }
}
