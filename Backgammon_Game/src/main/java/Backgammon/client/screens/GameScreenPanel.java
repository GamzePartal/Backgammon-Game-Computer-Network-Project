package Backgammon.client.screens;

import Backgammon.client.BackgammonClient;
import Backgammon.client.BoardRenderer;
import Backgammon.client.ScreenManager;
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

    private BoardRenderer renderer;

    private int selectedFromPoint = -2;
    private int selectedDieValue = -1;

    private List<Integer> validTargets;

    private boolean myTurn = false;

    private BoardPanel boardPanel;

    private JButton rollDiceButton;
    private JButton menuButton;
    private JButton undoButton;

    private JTextField chatInputField;
    private JButton chatSendButton;
    private JTextArea chatArea;

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
        this.renderer = new BoardRenderer();
        this.validTargets = new ArrayList<>();
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
            URL bgUrl = getClass().getResource("src/main/resources/images/arkaplanfoto.jpg");

            if (bgUrl != null) {
                backgroundImage = ImageIO.read(bgUrl);
                return;
            }

            File file1 = new File("src/images/arkaplanfoto.jpg");
            if (file1.exists()) {
                backgroundImage = ImageIO.read(file1);
                return;
            }

            File file2 = new File("src/main/resources/images/arkaplanfoto.jpg");
            if (file2.exists()) {
                backgroundImage = ImageIO.read(file2);
            }

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
        localPlayerCard = new PlayerCardPanel("Sen", "?", new Color(255, 215, 0), true);

        remotePlayerLabel = createPlayerLabel("Rakip", new Color(170, 170, 170));
        localPlayerLabel = createPlayerLabel("Sen", new Color(120, 190, 85));

        diceLabel = new JLabel("Zar: -");
        diceLabel.setFont(new Font("Arial", Font.BOLD, 19));
        diceLabel.setForeground(new Color(245, 220, 130));
        diceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        movesLabel = new JLabel("Kalan hamle: -");
        movesLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        movesLabel.setForeground(new Color(240, 230, 210));
        movesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rollDiceButton = createStyledButton("Zar At", new Color(75, 145, 70));
        rollDiceButton.setEnabled(false);
        rollDiceButton.addActionListener(e -> onRollDiceClicked());

        undoButton = createStyledButton("Hamle Geri Al", new Color(100, 80, 155));
        undoButton.setEnabled(false);
        undoButton.addActionListener(e -> onUndoClicked());

        menuButton = createStyledButton("Ana Menü", new Color(145, 65, 55));
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

        panel.add(remotePlayerCard, BorderLayout.NORTH);
        panel.add(centerControls, BorderLayout.CENTER);
        panel.add(localPlayerCard, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createPlayerLabel(String prefix, Color color) {
        JLabel label = new JLabel(prefix + ": ...");
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(28, 18, 10));
        panel.setMaximumSize(new Dimension(220, 230));

        JLabel chatTitle = new JLabel("💬 Sohbet");
        chatTitle.setFont(new Font("Arial", Font.BOLD, 14));
        chatTitle.setForeground(new Color(220, 185, 115));

        chatArea = new JTextArea(8, 18);
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));
        chatArea.setBackground(new Color(18, 12, 8));
        chatArea.setForeground(new Color(230, 220, 200));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(115, 85, 50), 1));

        chatInputField = new JTextField();
        chatInputField.setFont(new Font("Arial", Font.PLAIN, 12));
        chatInputField.setBackground(new Color(35, 24, 15));
        chatInputField.setForeground(Color.WHITE);
        chatInputField.setCaretColor(Color.WHITE);
        chatInputField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(130, 95, 55), 1),
                        BorderFactory.createEmptyBorder(5, 7, 5, 7)
                )
        );

        chatInputField.addActionListener(e -> onChatSend());

        chatSendButton = new JButton("➤");
        chatSendButton.setFont(new Font("Arial", Font.BOLD, 13));
        chatSendButton.setBackground(new Color(75, 145, 70));
        chatSendButton.setForeground(Color.WHITE);
        chatSendButton.setFocusPainted(false);
        chatSendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chatSendButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                )
        );

        chatSendButton.addActionListener(e -> onChatSend());

        JPanel inputRow = new JPanel(new BorderLayout(5, 0));
        inputRow.setBackground(new Color(28, 18, 10));
        inputRow.add(chatInputField, BorderLayout.CENTER);
        inputRow.add(chatSendButton, BorderLayout.EAST);

        panel.add(chatTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputRow, BorderLayout.SOUTH);

        return panel;
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

                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color bg;

                if (!isEnabled()) {
                    bg = new Color(90, 90, 90);
                } else if (getModel().isPressed()) {
                    bg = color.darker();
                } else if (getModel().isRollover()) {
                    bg = color.brighter();
                } else {
                    bg = color;
                }

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
        this.client = client;
        this.gameState = state;

        String myUsername = client.getUsername();

        if (state.getCurrentPlayer().getUsername().equals(myUsername)) {
            localPlayer = state.getCurrentPlayer();
            remotePlayer = state.getWaitingPlayer();
        } else {
            localPlayer = state.getWaitingPlayer();
            remotePlayer = state.getCurrentPlayer();
        }

        client.setPlayerID(localPlayer.getPlayerID());

        updatePlayerLabelsAndCards();

        updateBoard(state);
    }

    public void updateBoard(GameState state) {
        this.gameState = state;

        if (localPlayer != null) {
            int myID = localPlayer.getPlayerID();

            if (state.getCurrentPlayer() != null
                    && state.getCurrentPlayer().getPlayerID() == myID) {
                localPlayer = state.getCurrentPlayer();
                remotePlayer = state.getWaitingPlayer();
            } else if (state.getWaitingPlayer() != null
                    && state.getWaitingPlayer().getPlayerID() == myID) {
                localPlayer = state.getWaitingPlayer();
                remotePlayer = state.getCurrentPlayer();
            }
        }

        myTurn = localPlayer != null
                && state.getCurrentPlayer() != null
                && state.getCurrentPlayer().getPlayerID() == localPlayer.getPlayerID();

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
                localPlayerCard.updatePlayer(
                        localPlayer.getUsername(),
                        localPlayer.getPiecesBorneOff(),
                        colorStr
                );
            }
        }

        if (remotePlayer != null) {
            String colorStr = remotePlayer.getColor() == Player.WHITE ? "(Beyaz)" : "(Siyah)";
            remotePlayerLabel.setText("Rakip: " + remotePlayer.getUsername() + " " + colorStr);

            if (remotePlayerCard != null) {
                remotePlayerCard.updatePlayer(
                        remotePlayer.getUsername(),
                        remotePlayer.getPiecesBorneOff(),
                        colorStr
                );
            }
        }
    }

    private void updateDiceLabel(Dice dice) {
        if (dice == null || !dice.isRolled()) {
            diceLabel.setText("Zar: -");
        } else {
            String doublesStr = dice.isDoubles() ? " (ÇİFT!)" : "";

            diceLabel.setText(
                    "Zar: "
                            + dice.getDie1()
                            + " - "
                            + dice.getDie2()
                            + doublesStr
            );
        }
    }

    public void onDiceResult(GameMessage message) {
    }

    public void onTurnChange(GameMessage message) {
    }

    public void onChatReceived(GameMessage message) {
    }

    private void onChatSend() {
    }

    private void onRollDiceClicked() {
        if (client != null && myTurn) {
            client.sendRollDice();
            rollDiceButton.setEnabled(false);
        }
    }

    private void onUndoClicked() {
        JOptionPane.showMessageDialog(
                this,
                "Hamle geri alma özelliği yakında eklenecek.",
                "Hamle Geri Al",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void onPointClicked(int pointIndex) {
        if (!myTurn || gameState == null || !gameState.isDiceRolled()) {
            return;
        }

        Board board = gameState.getBoard();
        Dice dice = gameState.getDice();
        int color = localPlayer.getColor();

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
                if (pointIndex < 0 || pointIndex >= Board.POINT_COUNT) {
                    return;
                }

                Point p = board.getPoint(pointIndex);

                if (p == null || p.getOwner() != color || p.isEmpty()) {
                    return;
                }

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

            client.sendMove(selectedFromPoint, pointIndex, dieVal);

            clearSelection();

        } else if (validTargets.contains(-2) && pointIndex == selectedFromPoint) {
            client.sendMove(selectedFromPoint, -2, 0);
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

    private List<Integer> calculateBarEntries(Dice dice) {
        List<Integer> targets = new ArrayList<>();

        if (dice == null || localPlayer == null) {
            return targets;
        }

        for (int dieVal : dice.getRemainingMoves()) {
            int target;

            if (localPlayer.getColor() == Player.WHITE) {
                target = Board.POINT_COUNT - dieVal;
            } else {
                target = dieVal - 1;
            }

            if (target >= 0 && target < Board.POINT_COUNT) {
                Point p = gameState.getBoard().getPoint(target);

                if (p != null && p.isOpenFor(localPlayer.getColor())) {
                    if (!targets.contains(target)) {
                        targets.add(target);
                    }
                }
            }
        }

        return targets;
    }

    private List<Integer> calculateTargetsFor(int from, Dice dice) {
        List<Integer> targets = new ArrayList<>();

        if (dice == null || localPlayer == null) {
            return targets;
        }

        Board board = gameState.getBoard();
        boolean bearingOff = board.canBearOff(localPlayer);

        for (int dieVal : dice.getRemainingMoves()) {
            int target = from + dieVal * localPlayer.getDirection();

            if (target >= 0 && target < Board.POINT_COUNT) {
                Point p = gameState.getBoard().getPoint(target);

                if (p != null && p.isOpenFor(localPlayer.getColor())) {
                    if (!targets.contains(target)) {
                        targets.add(target);
                    }
                }
            } else if (bearingOff) {
                if (board.isValidMove(from, -2, dice, localPlayer) && !targets.contains(-2)) {
                    targets.add(-2);
                }

                break;
            }
        }

        return targets;
    }

    private int getBarEntryDieValue(int target) {
        if (localPlayer.getColor() == Player.WHITE) {
            return Board.POINT_COUNT - target;
        } else {
            return target + 1;
        }
    }

    private void clearSelection() {
        selectedFromPoint = -2;
        selectedDieValue = -1;
        validTargets.clear();
        renderer.clearSelection();
    }

    private void onMenuClicked() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Oyundan çıkmak istediğinize emin misiniz?",
                "Ana Menü",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            screenManager.showStartScreen();
        }
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
                g2d.drawString(
                        "Oyun yükleniyor...",
                        getWidth() / 2 - 90,
                        getHeight() / 2
                );
                return;
            }

            Player p1 = gameState.getCurrentPlayer();
            Player p2 = gameState.getWaitingPlayer();

            renderer.render(
                    g2d,
                    gameState.getBoard(),
                    gameState.getDice(),
                    p1,
                    p2
            );
        }

        private void handleBoardClick(int x, int y) {
            if (localPlayer != null
                    && localPlayer.hasBarPiece()
                    && renderer.isBarClicked(x, y)) {
                onPointClicked(-1);
                return;
            }

            int pointIndex = renderer.getPointIndexAt(x, y);

            if (pointIndex >= 0) {
                onPointClicked(pointIndex);
            }
        }
    }

    private class PlayerCardPanel extends JPanel {

        private String title;
        private String username;
        private int score;
        private Color avatarBorderColor;
        private boolean local;
        private String colorStr = "";

        public PlayerCardPanel(String title, String username, Color avatarBorderColor, boolean local) {
            this.title = title;
            this.username = username;
            this.avatarBorderColor = avatarBorderColor;
            this.local = local;
            this.score = 0;
            setPreferredSize(new Dimension(220, 95));
            setMaximumSize(new Dimension(220, 95));
            setOpaque(false);
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        public void updatePlayer(String username, int score) {
            this.username = username;
            this.score = score;
            repaint();
        }

        public void updatePlayer(String username, int score, String colorStr) {
            this.username = username;
            this.score = score;
            this.colorStr = colorStr;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            GradientPaint bg = new GradientPaint(
                    0,
                    0,
                    new Color(95, 60, 30, 225),
                    w,
                    h,
                    new Color(42, 24, 12, 225)
            );

            g2d.setPaint(bg);
            g2d.fillRoundRect(0, 0, w, h, 18, 18);

            g2d.setColor(new Color(155, 105, 60));
            g2d.setStroke(new BasicStroke(1.8f));
            g2d.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            int avatarSize = 42;
            int avatarX = 10;
            int avatarY = (h - avatarSize) / 2;

            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillOval(avatarX + 2, avatarY + 3, avatarSize, avatarSize);

            g2d.setColor(avatarBorderColor);
            g2d.fillOval(avatarX - 3, avatarY - 3, avatarSize + 6, avatarSize + 6);

            g2d.setColor(local ? new Color(40, 150, 200) : new Color(70, 120, 180));
            g2d.fillOval(avatarX, avatarY, avatarSize, avatarSize);

            drawAvatarFace(g2d, avatarX, avatarY, avatarSize, local);

            int textX = avatarX + avatarSize + 11;
            int lineH = 18;
            int startY = h / 2 - lineH;

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.setColor(new Color(200, 175, 120));
            g2d.drawString(title, textX, startY);

            String shownName = username == null ? "?" : username;

            if (shownName.length() > 9) {
                shownName = shownName.substring(0, 9) + "..";
            }

            String fullName = shownName
                    + (colorStr != null && !colorStr.isEmpty() ? " " + colorStr : "");

            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.setColor(new Color(245, 230, 200));
            g2d.drawString(fullName, textX, startY + lineH);

            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.setColor(new Color(160, 215, 120));
            g2d.drawString("Score = " + score, textX, startY + lineH * 2);

            g2d.dispose();
        }

        private void drawAvatarFace(Graphics2D g2d, int x, int y, int size, boolean local) {
            Color skin = new Color(255, 220, 185);
            Color hair = local ? new Color(45, 25, 20) : new Color(25, 20, 18);

            g2d.setColor(skin);
            g2d.fillOval(x + 11, y + 10, 20, 22);

            g2d.setColor(hair);
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