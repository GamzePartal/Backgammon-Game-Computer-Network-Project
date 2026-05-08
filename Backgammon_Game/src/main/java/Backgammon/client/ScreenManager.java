package Backgammon.client;
import Backgammon.client.screens.EndScreenPanel;
import Backgammon.client.screens.GameScreenPanel;
import Backgammon.client.screens.StartScreenPanel;
import Backgammon.common.GameMessage;
import Backgammon.common.GameState;
 
import javax.swing.*;
import java.awt.*;

public class ScreenManager {
   private final JFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
 
    private StartScreenPanel startScreen;
    private GameScreenPanel  gameScreen;
    private EndScreenPanel   endScreen;
 
    private BackgammonClient client;
 
    private static final String SCREEN_START = "START";
    private static final String SCREEN_GAME  = "GAME";
    private static final String SCREEN_END   = "END";
 
   
    public ScreenManager(JFrame mainFrame) {
        this.mainFrame  = mainFrame;
        this.cardLayout = new CardLayout();
        this.mainPanel  = new JPanel(cardLayout);
        initScreens();
    }
 
    private void initScreens() {
        startScreen = new StartScreenPanel(this);
        mainPanel.add(startScreen, SCREEN_START);
 
        gameScreen = new GameScreenPanel(this);
        mainPanel.add(gameScreen, SCREEN_GAME);
 
        endScreen = new EndScreenPanel(this);
        mainPanel.add(endScreen, SCREEN_END);
 
        cardLayout.show(mainPanel, SCREEN_START);
    }
 
    
    public void showStartScreen() {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        startScreen.reset();
        cardLayout.show(mainPanel, SCREEN_START);
    }
 
    public void showGameScreen() {
        cardLayout.show(mainPanel, SCREEN_GAME);
    }
 
    public void showEndScreen(String winnerName, boolean isLocalWinner) {
        endScreen.setResult(winnerName, isLocalWinner);
        cardLayout.show(mainPanel, SCREEN_END);
    }
 
    
    public void onMessageReceived(GameMessage message) {
        SwingUtilities.invokeLater(() -> processMessage(message));
    }
 
    private void processMessage(GameMessage message) {
        switch (message.getType()) {
 
            case GAME_START:
                handleGameStart(message);
                break;
 
            case BOARD_UPDATE:
                handleBoardUpdate(message);
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
                handleError(message);
                break;
 
            case PLAYER_DISCONNECT:
                handlePlayerDisconnect(message);
                break;
 
            case WAITING:
                startScreen.showWaitingMessage();
                break;
 
            default:
                System.out.println("[EKRAN] Islenmeyen mesaj tipi: " + message.getType());
                break;
        }
    }
 
    private void handleGameStart(GameMessage message) {
        if (message.getData() instanceof GameState) {
            GameState state = (GameState) message.getData();
            // playerID gameScreen.initGame() içinde set edilir (localPlayer üzerinden)
            gameScreen.initGame(state, client);
            showGameScreen();
        }
    }
 
    private void handleBoardUpdate(GameMessage message) {
        if (message.getData() instanceof GameState) {
            GameState state = (GameState) message.getData();
            gameScreen.updateBoard(state);
        }
    }
 
    private void handleGameOver(GameMessage message) {
        if (message.getData() instanceof GameState) {
            GameState state = (GameState) message.getData();
            String winnerName = (state.getWinner() != null)
                    ? state.getWinner().getUsername() : "Bilinmiyor";
            boolean isLocalWinner = (state.getWinner() != null)
                    && state.getWinner().getPlayerID() == client.getPlayerID();
            showEndScreen(winnerName, isLocalWinner);
        }
    }
 
    private void handleError(GameMessage message) {
        String errorText = (message.getData() instanceof String)
                ? (String) message.getData() : "Bilinmeyen hata";
        JOptionPane.showMessageDialog(mainFrame, errorText, "Hata", JOptionPane.ERROR_MESSAGE);
    }
 
    private void handlePlayerDisconnect(GameMessage message) {
        JOptionPane.showMessageDialog(mainFrame,
                "Rakibiniz oyundan ayrildi. Ana menuye donuluyor...",
                "Baglanti Kesildi", JOptionPane.WARNING_MESSAGE);
        showStartScreen();
    }
 
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainFrame,
                    "Sunucu baglantisi kesildi!",
                    "Baglanti Hatasi", JOptionPane.ERROR_MESSAGE);
            showStartScreen();
        });
    }
 
 
    public boolean connectToServer(String ip, int port, String username) {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
 
        client = new BackgammonClient(this);
        boolean connected = client.connect(ip, port);
 
        if (connected) {
            client.sendPlayerJoin(username);
        }
 
        return connected;
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