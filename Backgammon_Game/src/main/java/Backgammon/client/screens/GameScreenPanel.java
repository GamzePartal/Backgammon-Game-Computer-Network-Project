package Backgammon.client.screens;

import Backgammon.client.BackgammonClient;
import Backgammon.client.BoardRenderer;
import Backgammon.client.ScreenManager;
import Backgammon.client.SoundManager;
import Backgammon.common.Board;
import Backgammon.common.Dice;
import Backgammon.common.GameMessage;
import Backgammon.common.GameState;
import Backgammon.common.Player;
import Backgammon.common.Point;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GameScreenPanel extends JPanel {

    private final ScreenManager screenManager;
    private BackgammonClient client;

    private GameState gameState;
    private Player localPlayer;
    private Player remotePlayer;

    // localPlayerID: initGame'de bir kez set edilir, sonraki güncellemelerde referans noktasıdır
    private int localPlayerID = -1;

    private BoardRenderer renderer;

    private int selectedFromPoint = -2;
    private int selectedDieValue  = -1;

    private List<Integer> validTargets;

    private boolean myTurn = false;

    // Bar takibi: taş kırılma sesini tetiklemek için
    private int prevRemoteBarCount = 0;

    private BoardPanel boardPanel;

    private JButton rollDiceButton;
    private JButton menuButton;
    private JButton undoButton;

    private JLabel statusLabel;
    private JLabel diceLabel;
    private JLabel movesLabel;

    private JLabel localPlayerLabel;
    private JLabel remotePlayerLabel;

    private PlayerCardPanel remotePlayerCard;
    private PlayerCardPanel localPlayerCard;

    private Image backgroundImage;

    public GameScreenPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.renderer      = new BoardRenderer();
        this.validTargets  = new ArrayList<>();
        initUI();
    }


    private void initUI() {
        loadBackgroundImage();

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(930, 600));
        boardPanel.setOpaque(false);
        add(boardPanel, BorderLayout.CENTER);

        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void loadBackgroundImage() {
        try {
            URL bgUrl = getClass().getResource("/images/arkaplanfoto.jpg");
            if (bgUrl != null) { backgroundImage = ImageIO.read(bgUrl); return; }
            File f1 = new File("src/images/arkaplanfoto.jpg");
            if (f1.exists()) { backgroundImage = ImageIO.read(f1); return; }
            File f2 = new File("src/main/resources/images/arkaplanfoto.jpg");
            if (f2.exists()) { backgroundImage = ImageIO.read(f2); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(new Color(22, 14, 8));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        g2d.setColor(new Color(0, 0, 0, 125));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 8, 12, 8));
        panel.setPreferredSize(new Dimension(240, 600));

        remotePlayerCard = new PlayerCardPanel("Rakip", "?", new Color(50, 150, 220), false);
        localPlayerCard  = new PlayerCardPanel("Sen",   "?", new Color(255, 215, 0),  true);

        remotePlayerLabel = createPlayerLabel("Rakip", new Color(170, 170, 170));
        localPlayerLabel  = createPlayerLabel("Sen",   new Color(120, 190, 85));

        diceLabel = new JLabel("Zar: -");
        diceLabel.setFont(new Font("Arial", Font.BOLD, 19));
        diceLabel.setForeground(new Color(245, 220, 130));
        diceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        movesLabel = new JLabel("Kalan hamle: -");
        movesLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        movesLabel.setForeground(new Color(240, 230, 210));
        movesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rollDiceButton = createStyledButton("Zar At",        new Color(75, 145, 70));
        rollDiceButton.setEnabled(false);
        rollDiceButton.addActionListener(e -> onRollDiceClicked());

        undoButton = createStyledButton("Hamle Geri Al",  new Color(100, 80, 155));
        undoButton.setEnabled(false);
        undoButton.addActionListener(e -> onUndoClicked());

        menuButton = createStyledButton("Ana Menü",       new Color(145, 65, 55));
        menuButton.addActionListener(e -> onMenuClicked());

        JPanel centerControls = new JPanel();
        centerControls.setOpaque(false);
        centerControls.setLayout(new BoxLayout(centerControls, BoxLayout.Y_AXIS));
        centerControls.add(Box.createVerticalGlue());
        centerControls.add(diceLabel);
        centerControls.add(Box.createVerticalStrut(5));
        centerControls.add(movesLabel);
        centerControls.add(Box.createVerticalStrut(12));
        centerControls.add(rollDiceButton);
        centerControls.add(Box.createVerticalStrut(7));
        centerControls.add(undoButton);
        centerControls.add(Box.createVerticalStrut(7));
        centerControls.add(menuButton);
        centerControls.add(Box.createVerticalGlue());

        panel.add(remotePlayerCard,  BorderLayout.NORTH);
        panel.add(centerControls,    BorderLayout.CENTER);
        panel.add(localPlayerCard,   BorderLayout.SOUTH);
        return panel;
    }

    private JLabel createPlayerLabel(String prefix, Color color) {
        JLabel label = new JLabel(prefix + ": ...");
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panel.setOpaque(false);
        statusLabel = new JLabel("Oyun yükleniyor...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 15));
        statusLabel.setForeground(new Color(245, 220, 130));
        panel.add(statusLabel);
        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg;
                if (!isEnabled())                bg = new Color(90, 90, 90);
                else if (getModel().isPressed())  bg = color.darker();
                else if (getModel().isRollover()) bg = color.brighter();
                else                             bg = color;
                g2d.setColor(new Color(0, 0, 0, 90));
                g2d.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, 14, 14);
                g2d.setColor(bg);
                g2d.fillRoundRect(2, 2, getWidth() - 6, getHeight() - 7, 14, 14);
                g2d.setColor(new Color(255, 255, 255, 75));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(4, 4, getWidth() - 10, getHeight() - 11, 12, 12);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(165, 28));
        button.setPreferredSize(new Dimension(165, 28));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    
    public void initGame(GameState state, BackgammonClient client) {
        this.client    = client;
        this.gameState = state;

        String myUsername = client.getUsername();

        if (state.getCurrentPlayer().getUsername().equals(myUsername)) {
            localPlayer  = state.getCurrentPlayer();
            remotePlayer = state.getWaitingPlayer();
        } else {
            localPlayer  = state.getWaitingPlayer();
            remotePlayer = state.getCurrentPlayer();
        }

        localPlayerID = localPlayer.getPlayerID();
        client.setPlayerID(localPlayerID);

        // Bar sayacını sıfırla
        prevRemoteBarCount = 0;

        // Arkaplan müziğini başlat (rematch'te zaten çalıyorsa tekrar başlamaz)
        SoundManager.getInstance().playBackground();

        updatePlayerLabelsAndCards();
        updateBoard(state);

        // Başlangıç zarlarını oyuncuya göster
        showInitRollDialog(state);
    }


    private void showInitRollDialog(GameState state) {
        int roll1 = state.getInitRollPlayer1();
        int roll2 = state.getInitRollPlayer2();
        if (roll1 == 0 || roll2 == 0) return;

        Player white = (state.getCurrentPlayer().getColor() == Player.WHITE)
                ? state.getCurrentPlayer() : state.getWaitingPlayer();
        Player black = (state.getCurrentPlayer().getColor() == Player.BLACK)
                ? state.getCurrentPlayer() : state.getWaitingPlayer();

        String whiteName   = white.getUsername();
        String blackName   = black.getUsername();
        String starterName = state.getCurrentPlayer().getUsername();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(45, 28, 12));
        panel.setBorder(new EmptyBorder(18, 30, 18, 30));

        JLabel title = new JLabel("🎲 Başlangıç Zar Atışı");
        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(new Color(255, 210, 80));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sep = new JLabel("─────────────────────");
        sep.setForeground(new Color(150, 110, 60));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel whiteRow = new JLabel(String.format("⬜ %s   →   %d", whiteName, roll1));
        whiteRow.setFont(new Font("Arial", Font.BOLD, 16));
        whiteRow.setForeground(new Color(240, 235, 220));
        whiteRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel blackRow = new JLabel(String.format("⬛ %s   →   %d", blackName, roll2));
        blackRow.setFont(new Font("Arial", Font.BOLD, 16));
        blackRow.setForeground(new Color(200, 190, 175));
        blackRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sep2 = new JLabel("─────────────────────");
        sep2.setForeground(new Color(150, 110, 60));
        sep2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel winner = new JLabel("🏆  " + starterName + " başlıyor!");
        winner.setFont(new Font("Georgia", Font.BOLD, 17));
        winner.setForeground(new Color(120, 230, 100));
        winner.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));
        panel.add(whiteRow);
        panel.add(Box.createVerticalStrut(8));
        panel.add(blackRow);
        panel.add(Box.createVerticalStrut(12));
        panel.add(sep2);
        panel.add(Box.createVerticalStrut(10));
        panel.add(winner);

        UIManager.put("OptionPane.background",        new Color(45, 28, 12));
        UIManager.put("Panel.background",             new Color(45, 28, 12));
        UIManager.put("OptionPane.messageForeground", new Color(240, 230, 200));

        JOptionPane.showMessageDialog(
                screenManager.getMainFrame(), panel,
                "Başlangıç Zar Atışı", JOptionPane.PLAIN_MESSAGE);

        UIManager.put("OptionPane.background",        null);
        UIManager.put("Panel.background",             null);
        UIManager.put("OptionPane.messageForeground", null);
    }

    
    public void updateBoard(GameState state) {
        this.gameState = state;

        if (localPlayerID != -1) {
            if (state.getCurrentPlayer() != null
                    && state.getCurrentPlayer().getPlayerID() == localPlayerID) {
                localPlayer  = state.getCurrentPlayer();
                remotePlayer = state.getWaitingPlayer();
            } else if (state.getWaitingPlayer() != null
                    && state.getWaitingPlayer().getPlayerID() == localPlayerID) {
                localPlayer  = state.getWaitingPlayer();
                remotePlayer = state.getCurrentPlayer();
            }
        }

        // Taş kırılma sesi: rakibin bar sayısı önceki duruma göre artmışsa
        if (remotePlayer != null) {
            int currentRemoteBar = remotePlayer.getPiecesOnBar();
            if (currentRemoteBar > prevRemoteBarCount) {
                SoundManager.getInstance().playPieceHit();
            }
            prevRemoteBarCount = currentRemoteBar;
        }

        myTurn = localPlayer != null
                && state.getCurrentPlayer() != null
                && state.getCurrentPlayer().getPlayerID() == localPlayerID;

        clearSelection();

        rollDiceButton.setEnabled(myTurn && !state.isDiceRolled());

        updateDiceLabel(state.getDice());

        if (state.getDice() != null && state.getDice().isRolled()) {
            movesLabel.setText("Kalan hamle: " + state.getDice().getRemainingCount());
        } else {
            movesLabel.setText("Kalan hamle: -");
        }

        updatePlayerLabelsAndCards();
        statusLabel.setText(state.getStatusMessage());
        boardPanel.repaint();
    }

    private void updatePlayerLabelsAndCards() {
        if (localPlayer != null) {
            String colorStr = localPlayer.getColor() == Player.WHITE ? "(Beyaz)" : "(Siyah)";
            localPlayerLabel.setText("Sen: " + localPlayer.getUsername() + " " + colorStr);
            if (localPlayerCard != null) {
                localPlayerCard.updatePlayer(localPlayer.getUsername(),
                        localPlayer.getWins(), colorStr);
            }
        }
        if (remotePlayer != null) {
            String colorStr = remotePlayer.getColor() == Player.WHITE ? "(Beyaz)" : "(Siyah)";
            remotePlayerLabel.setText("Rakip: " + remotePlayer.getUsername() + " " + colorStr);
            if (remotePlayerCard != null) {
                remotePlayerCard.updatePlayer(remotePlayer.getUsername(),
                        remotePlayer.getWins(), colorStr);
            }
        }
    }

    private void updateDiceLabel(Dice dice) {
        if (dice == null || !dice.isRolled()) {
            diceLabel.setText("Zar: -");
        } else {
            String doublesStr = dice.isDoubles() ? " (ÇİFT!)" : "";
            diceLabel.setText("Zar: " + dice.getDie1() + " - " + dice.getDie2() + doublesStr);
        }
    }

    

    public void onDiceResult(GameMessage message) {
        if (message.getData() instanceof GameState) {
            updateBoard((GameState) message.getData());
        } else if (message.getData() instanceof int[]) {
            int[] vals = (int[]) message.getData();
            if (vals.length >= 2) {
                String doublesStr = (vals[0] == vals[1]) ? " (ÇİFT!)" : "";
                diceLabel.setText("Zar: " + vals[0] + " - " + vals[1] + doublesStr);
            }
        }
    }

    public void onTurnChange(GameMessage message) {
        if (message.getData() instanceof GameState) {
            updateBoard((GameState) message.getData());
        }
    }

   

    private void onRollDiceClicked() {
        if (client != null && myTurn) {
            // Zar atma sesi
            SoundManager.getInstance().playDiceRoll();
            client.sendRollDice();
            rollDiceButton.setEnabled(false);
        }
    }

    private void onUndoClicked() {
        JOptionPane.showMessageDialog(this,
                "Hamle geri alma özelliği yakında eklenecek.",
                "Hamle Geri Al", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onMenuClicked() {
        int result = JOptionPane.showConfirmDialog(this,
                "Oyundan çıkmak istediğinize emin misiniz?",
                "Ana Menü", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            // Ana menüye dönerken arkaplan müziğini durdur
            SoundManager.getInstance().stopBackground();
            screenManager.showStartScreen();
        }
    }

    
    private void onPointClicked(int pointIndex) {
        if (!myTurn || gameState == null || !gameState.isDiceRolled()) return;

        Board board = gameState.getBoard();
        Dice  dice  = gameState.getDice();
        int   color = localPlayer.getColor();

        if (localPlayer.hasBarPiece() && pointIndex != -1 && selectedFromPoint != -1) {
            statusLabel.setText("Önce bardaki taşını oyna! Ortadaki bara tıkla.");
            return;
        }

        if (selectedFromPoint == -2) {
            if (localPlayer.hasBarPiece()) {
                selectedFromPoint = -1;
                validTargets = calculateBarEntries(dice);
                if (validTargets.isEmpty()) {
                    statusLabel.setText("Bar'dan girilebilecek hane yok, sıra geçiyor...");
                    clearSelection();
                    return;
                }
            } else {
                if (pointIndex < 0 || pointIndex >= Board.POINT_COUNT) return;
                Point p = board.getPoint(pointIndex);
                if (p == null || p.getOwner() != color || p.isEmpty()) return;
                selectedFromPoint = pointIndex;
                validTargets = calculateTargetsFor(pointIndex, dice);
            }
            renderer.setSelectedPoint(selectedFromPoint);
            renderer.setHighlightedPoints(validTargets);
            boardPanel.repaint();
            return;
        }

        if (validTargets.contains(pointIndex)) {
            int dieVal = selectedFromPoint == -1
                    ? getBarEntryDieValue(pointIndex)
                    : Math.abs(pointIndex - selectedFromPoint);
            // Taş hamlesi sesi
            SoundManager.getInstance().playPieceMove();
            client.sendMove(selectedFromPoint, pointIndex, dieVal);
            clearSelection();

        } else if (validTargets.contains(-2) && pointIndex == selectedFromPoint) {
            int dieVal = findBearingOffDieValue(selectedFromPoint, dice);
            // Taş toplama sesi (aynı hamle sesi)
            SoundManager.getInstance().playPieceMove();
            client.sendMove(selectedFromPoint, -2, dieVal);
            clearSelection();

        } else if (pointIndex == selectedFromPoint) {
            clearSelection();

        } else {
            clearSelection();
            onPointClicked(pointIndex);
            return;
        }

        boardPanel.repaint();
    }

    private int findBearingOffDieValue(int from, Dice dice) {
        for (int dieVal : dice.getRemainingMoves()) {
            int dist = (localPlayer.getColor() == Player.WHITE)
                    ? from + 1
                    : Board.POINT_COUNT - from;
            if (dieVal == dist || dieVal > dist) return dieVal;
        }
        return dice.getRemainingMoves().isEmpty() ? 0 : dice.getRemainingMoves().get(0);
    }

    private List<Integer> calculateBarEntries(Dice dice) {
        List<Integer> targets = new ArrayList<>();
        if (dice == null || localPlayer == null) return targets;
        for (int dieVal : dice.getRemainingMoves()) {
            int target = (localPlayer.getColor() == Player.WHITE)
                    ? Board.POINT_COUNT - dieVal : dieVal - 1;
            if (target >= 0 && target < Board.POINT_COUNT) {
                Point p = gameState.getBoard().getPoint(target);
                if (p != null && p.isOpenFor(localPlayer.getColor())
                        && !targets.contains(target)) {
                    targets.add(target);
                }
            }
        }
        return targets;
    }

    private List<Integer> calculateTargetsFor(int from, Dice dice) {
        List<Integer> targets = new ArrayList<>();
        if (dice == null || localPlayer == null) return targets;
        Board board        = gameState.getBoard();
        boolean bearingOff = board.canBearOff(localPlayer);
        for (int dieVal : dice.getRemainingMoves()) {
            int target = from + dieVal * localPlayer.getDirection();
            if (target >= 0 && target < Board.POINT_COUNT) {
                Point p = board.getPoint(target);
                if (p != null && p.isOpenFor(localPlayer.getColor())
                        && !targets.contains(target)) {
                    targets.add(target);
                }
            } else if (bearingOff) {
                if (board.isValidMove(from, -2, dice, localPlayer)
                        && !targets.contains(-2)) {
                    targets.add(-2);
                }
                break;
            }
        }
        return targets;
    }

    private int getBarEntryDieValue(int target) {
        return (localPlayer.getColor() == Player.WHITE)
                ? Board.POINT_COUNT - target : target + 1;
    }

    private void clearSelection() {
        selectedFromPoint = -2;
        selectedDieValue  = -1;
        validTargets.clear();
        renderer.clearSelection();
    }

 

    private class BoardPanel extends JPanel {
        public BoardPanel() {
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleBoardClick(e.getX(), e.getY());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            if (gameState == null || gameState.getBoard() == null) {
                g2d.setColor(Color.GRAY);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString("Oyun yükleniyor...", getWidth() / 2 - 90, getHeight() / 2);
                return;
            }
            renderer.render(g2d, gameState.getBoard(), gameState.getDice(),
                    gameState.getCurrentPlayer(), gameState.getWaitingPlayer());
        }

        private void handleBoardClick(int x, int y) {
            if (localPlayer != null && localPlayer.hasBarPiece()
                    && renderer.isBarClicked(x, y)) {
                onPointClicked(-1);
                return;
            }
            int pointIndex = renderer.getPointIndexAt(x, y);
            if (pointIndex >= 0) onPointClicked(pointIndex);
        }
    }

   

    private class PlayerCardPanel extends JPanel {

        private String  title;
        private String  username;
        private int     score;
        private Color   avatarBorderColor;
        private boolean local;
        private String  colorStr = "";

        public PlayerCardPanel(String title, String username,
                               Color avatarBorderColor, boolean local) {
            this.title             = title;
            this.username          = username;
            this.avatarBorderColor = avatarBorderColor;
            this.local             = local;
            this.score             = 0;
            setPreferredSize(new Dimension(220, 95));
            setMaximumSize(new Dimension(220, 95));
            setOpaque(false);
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        public void updatePlayer(String username, int score) {
            this.username = username;
            this.score    = score;
            repaint();
        }

        public void updatePlayer(String username, int score, String colorStr) {
            this.username = username;
            this.score    = score;
            this.colorStr = colorStr;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            GradientPaint bg = new GradientPaint(
                    0, 0, new Color(95, 60, 30, 225),
                    w, h, new Color(42, 24, 12, 225));
            g2d.setPaint(bg);
            g2d.fillRoundRect(0, 0, w, h, 18, 18);
            g2d.setColor(new Color(155, 105, 60));
            g2d.setStroke(new BasicStroke(1.8f));
            g2d.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            int avatarSize = 42, avatarX = 10;
            int avatarY = (h - avatarSize) / 2;
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillOval(avatarX + 2, avatarY + 3, avatarSize, avatarSize);
            g2d.setColor(avatarBorderColor);
            g2d.fillOval(avatarX - 3, avatarY - 3, avatarSize + 6, avatarSize + 6);
            g2d.setColor(local ? new Color(40, 150, 200) : new Color(70, 120, 180));
            g2d.fillOval(avatarX, avatarY, avatarSize, avatarSize);
            drawAvatarFace(g2d, avatarX, avatarY, avatarSize, local);

            int textX = avatarX + avatarSize + 11;
            int lineH = 18, startY = h / 2 - lineH;

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.setColor(new Color(200, 175, 120));
            g2d.drawString(title, textX, startY);

            String shownName = (username == null) ? "?" : username;
            if (shownName.length() > 9) shownName = shownName.substring(0, 9) + "..";
            String fullName = shownName
                    + (colorStr != null && !colorStr.isEmpty() ? " " + colorStr : "");

            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.setColor(new Color(245, 230, 200));
            g2d.drawString(fullName, textX, startY + lineH);

            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.setColor(new Color(160, 215, 120));
            g2d.drawString("Galibiyet: " + score, textX, startY + lineH * 2);

            g2d.dispose();
        }

        private void drawAvatarFace(Graphics2D g2d, int x, int y, int size, boolean local) {
            g2d.setColor(new Color(255, 220, 185));
            g2d.fillOval(x + 11, y + 10, 20, 22);
            g2d.setColor(local ? new Color(45, 25, 20) : new Color(25, 20, 18));
            g2d.fillArc(x + 9, y + 8, 24, 16, 0, 180);
            g2d.setColor(Color.BLACK);
            g2d.fillOval(x + 15, y + 19, 3, 3);
            g2d.fillOval(x + 24, y + 19, 3, 3);
            if (local) {
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(x + 13, y + 17, 8, 6);
                g2d.drawOval(x + 22, y + 17, 8, 6);
                g2d.drawLine(x + 21, y + 20, x + 22, y + 20);
            } else {
                g2d.setStroke(new BasicStroke(1.8f));
                g2d.drawLine(x + 15, y + 27, x + 27, y + 27);
            }
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawArc(x + 17, y + 25, 9, 5, 180, 180);
        }
    }
}