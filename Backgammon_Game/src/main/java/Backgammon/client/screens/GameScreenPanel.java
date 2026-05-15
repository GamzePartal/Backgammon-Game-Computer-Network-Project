package Backgammon.client.screens;

import Backgammon.client.BackgammonClient;
import Backgammon.client.BoardRenderer;
import Backgammon.client.ScreenManager;
import Backgammon.client.SoundManager;
import Backgammon.common.*;
import Backgammon.common.Point;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//oyunun oynandığı ana ekrandır
//tahta, oyuncu kartları, zar butonu, durum mesajları ve hamle tıklamaları burada yönetilir
public class GameScreenPanel extends JPanel {

    private final ScreenManager screenManager;
    private final BoardRenderer renderer = new BoardRenderer();
    private BackgammonClient client;
    private GameState gameState;
    private Player localPlayer, remotePlayer;
    private int localPlayerID = -1;
    private int selectedFromPoint = -2;
    private int selectedDieValue = -1;
    private List<Integer> validTargets = new ArrayList<>();
    private boolean myTurn, waitingForServer;
    private int prevRemoteBarCount;
    private BoardPanel boardPanel;
    private JButton rollDiceButton;
    private JLabel statusLabel, diceLabel, movesLabel;
    private JLabel localPlayerLabel, remotePlayerLabel;
    private PlayerCardPanel remotePlayerCard, localPlayerCard;
    private Image backgroundImage;

    //oyun ekranı panelini oluşturur ve arayüzü başlatır
    public GameScreenPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        initUI();
    }

    private void initUI() {
        URL url = getClass().getResource("/images/arkaplanfoto.jpg");
        if (url != null) try {
            backgroundImage = ImageIO.read(url);
        } catch (IOException e) {
            System.err.println("[UI] Arkaplan yuklenemedi: " + e.getMessage());
        }

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(930, 600));
        boardPanel.setOpaque(false);
        add(boardPanel, BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
        add(buildStatusPanel(), BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(22, 14, 8));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.setColor(new Color(0, 0, 0, 125));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 8, 12, 8));
        panel.setPreferredSize(new Dimension(240, 600));

        remotePlayerCard = new PlayerCardPanel("Rakip", "?", new Color(50, 150, 220), false);
        localPlayerCard = new PlayerCardPanel("Sen", "?", new Color(255, 215, 0), true);
        remotePlayerLabel = playerLabel("Rakip", new Color(170, 170, 170));
        localPlayerLabel = playerLabel("Sen", new Color(120, 190, 85));

        diceLabel = infoLabel("Zar: -", new Font("Arial", Font.BOLD, 19), new Color(245, 220, 130));
        movesLabel = infoLabel("Kalan hamle: -", new Font("Arial", Font.PLAIN, 13), new Color(240, 230, 210));

        rollDiceButton = styledButton("Zar At", new Color(75, 145, 70));
        rollDiceButton.setEnabled(false);
        rollDiceButton.addActionListener(e -> onRollDice());
        JButton menuButton = styledButton("Ana Menü", new Color(145, 65, 55));
        menuButton.addActionListener(e -> onMenu());

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalGlue());
        center.add(diceLabel);
        center.add(Box.createVerticalStrut(5));
        center.add(movesLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(rollDiceButton);
        center.add(Box.createVerticalStrut(7));
        center.add(menuButton);
        center.add(Box.createVerticalGlue());

        panel.add(remotePlayerCard, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(localPlayerCard, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel playerLabel(String prefix, Color color) {
        JLabel l = new JLabel(prefix + ": ...");
        l.setFont(new Font("Arial", Font.BOLD, 15));
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JLabel infoLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JPanel buildStatusPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setOpaque(false);
        statusLabel = new JLabel("Oyun yükleniyor...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 15));
        statusLabel.setForeground(new Color(245, 220, 130));
        p.add(statusLabel);
        return p;
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled() ? new Color(90, 90, 90) : getModel().isPressed() ? color.darker() : getModel().isRollover() ? color.brighter() : color;
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, 14, 14);
                g2.setColor(bg);
                g2.fillRoundRect(2, 2, getWidth() - 6, getHeight() - 7, 14, 14);
                g2.setColor(new Color(255, 255, 255, 75));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(4, 4, getWidth() - 10, getHeight() - 11, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(165, 28));
        btn.setPreferredSize(new Dimension(165, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void initGame(GameState state, BackgammonClient client) {
        this.client = client;
        this.gameState = state;
        String me = client.getUsername();
        if (state.getCurrentPlayer().getUsername().equals(me)) {
            localPlayer = state.getCurrentPlayer();
            remotePlayer = state.getWaitingPlayer();
        } else {
            localPlayer = state.getWaitingPlayer();
            remotePlayer = state.getCurrentPlayer();
        }
        localPlayerID = localPlayer.getPlayerID();
        client.setPlayerID(localPlayerID);
        prevRemoteBarCount = 0;
        waitingForServer = false;
        renderer.setLocalPlayerColor(localPlayer.getColor());
        SoundManager.getInstance().playBackground();
        updateLabelsAndCards();
        updateBoard(state);
        showInitRollDialog(state);
    }

    private void showInitRollDialog(GameState state) {
        int r1 = state.getInitRollPlayer1(), r2 = state.getInitRollPlayer2();
        if (r1 == 0 || r2 == 0) {
            return;
        }

        Player white, black;
        if (state.getCurrentPlayer().getColor() == Player.WHITE) {
            white = state.getCurrentPlayer();
            black = state.getWaitingPlayer();
        } else {
            black = state.getCurrentPlayer();
            white = state.getWaitingPlayer();
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(45, 28, 12));
        panel.setBorder(new EmptyBorder(18, 30, 18, 30));

        for (JLabel lbl : new JLabel[]{
            dlg("Başlangıç Zar Atışı", new Font("Georgia", Font.BOLD, 18), new Color(255, 210, 80)),
            dlg("─────────────────────", null, new Color(150, 110, 60)),
            dlg(String.format("⬜ %s   →   %d", white != null ? white.getUsername() : "?", r1), new Font("Arial", Font.BOLD, 16), new Color(240, 235, 220)),
            dlg(String.format("⬛ %s   →   %d", black != null ? black.getUsername() : "?", r2), new Font("Arial", Font.BOLD, 16), new Color(200, 190, 175)),
            dlg("─────────────────────", null, new Color(150, 110, 60)),
            dlg(state.getCurrentPlayer().getUsername() + " başlıyor!", new Font("Georgia", Font.BOLD, 17), new Color(120, 230, 100))
        }) {
            panel.add(lbl);
            panel.add(Box.createVerticalStrut(10));
        }

        UIManager.put("OptionPane.background", new Color(45, 28, 12));
        UIManager.put("Panel.background", new Color(45, 28, 12));
        UIManager.put("OptionPane.messageForeground", new Color(240, 230, 200));
        JOptionPane.showMessageDialog(screenManager.getMainFrame(), panel, "Başlangıç Zar Atışı", JOptionPane.PLAIN_MESSAGE);
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageForeground", null);
    }

    private JLabel dlg(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        if (font != null) {
            l.setFont(font);
        }
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    public void updateBoard(GameState state) {
        this.gameState = state;
        if (localPlayerID != -1) {
            if (state.getCurrentPlayer() != null && state.getCurrentPlayer().getPlayerID() == localPlayerID) {
                localPlayer = state.getCurrentPlayer();
                remotePlayer = state.getWaitingPlayer();
            } else if (state.getWaitingPlayer() != null && state.getWaitingPlayer().getPlayerID() == localPlayerID) {
                localPlayer = state.getWaitingPlayer();
                remotePlayer = state.getCurrentPlayer();
            }
        }
        if (remotePlayer != null) {
            int cur = remotePlayer.getPiecesOnBar();
            if (cur > prevRemoteBarCount) {
                SoundManager.getInstance().playPieceHit();
            }
            prevRemoteBarCount = cur;
        }
        myTurn = localPlayer != null && state.getCurrentPlayer() != null
                && state.getCurrentPlayer().getPlayerID() == localPlayerID;
        waitingForServer = false;
        clearSelection();
        rollDiceButton.setEnabled(myTurn && !state.isDiceRolled());
        updateDiceLabel(state.getDice());
        movesLabel.setText(state.getDice() != null && state.getDice().isRolled()
                ? "Kalan hamle: " + state.getDice().getRemainingCount() : "Kalan hamle: -");
        updateLabelsAndCards();
        statusLabel.setText(state.getStatusMessage());
        boardPanel.repaint();
    }

    private void updateLabelsAndCards() {
        if (localPlayer != null) {
            String cs = localPlayer.getColor() == Player.WHITE ? "(Beyaz)" : "(Siyah)";
            localPlayerLabel.setText("Sen: " + localPlayer.getUsername() + " " + cs);
            if (localPlayerCard != null) {
                localPlayerCard.updatePlayer(localPlayer.getUsername(), localPlayer.getWins(), cs);
            }
        }
        if (remotePlayer != null) {
            String cs = remotePlayer.getColor() == Player.WHITE ? "(Beyaz)" : "(Siyah)";
            remotePlayerLabel.setText("Rakip: " + remotePlayer.getUsername() + " " + cs);
            if (remotePlayerCard != null) {
                remotePlayerCard.updatePlayer(remotePlayer.getUsername(), remotePlayer.getWins(), cs);
            }
        }
    }

    private void updateDiceLabel(Dice dice) {
        if (dice == null || !dice.isRolled()) {
            diceLabel.setText("Zar: -");
            return;
        }
        diceLabel.setText("Zar: " + dice.getDie1() + " - " + dice.getDie2() + (dice.isDoubles() ? " (ÇİFT!)" : ""));
    }

    public void onDiceResult(GameMessage msg) {
        if (msg.getData() instanceof GameState) {
            updateBoard((GameState) msg.getData());
        } else if (msg.getData() instanceof int[]) {
            int[] v = (int[]) msg.getData();
            if (v.length >= 2) {
                diceLabel.setText("Zar: " + v[0] + " - " + v[1] + (v[0] == v[1] ? " (ÇİFT!)" : ""));
            }
        }
    }

    public void onTurnChange(GameMessage msg) {
        if (msg.getData() instanceof GameState) {
            updateBoard((GameState) msg.getData());
        }
    }

    private void onRollDice() {
        if (client != null && myTurn) {
            SoundManager.getInstance().playDiceRoll();
            client.sendRollDice();
            rollDiceButton.setEnabled(false);
        }
    }

    private void onMenu() {
        if (JOptionPane.showConfirmDialog(this, "Oyundan çıkmak istediğinize emin misiniz?",
                "Ana Menü", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            SoundManager.getInstance().playButtonClick();
            SoundManager.getInstance().stopBackground();
            screenManager.showStartScreen();
        }
    }

    private void onPointClicked(int pointIndex) {
        if (waitingForServer || !myTurn || gameState == null || !gameState.isDiceRolled()) {
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
                validTargets = calcBarEntries(dice);
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
                validTargets = calcTargets(pointIndex, dice);
                if (validTargets.isEmpty()) {
                    statusLabel.setText("Bu taşla yapılabilecek hamle yok, başka taş seç.");
                    clearSelection();
                    return;
                }
            }
            renderer.setSelectedPoint(selectedFromPoint);
            renderer.setHighlightedPoints(validTargets);
            boardPanel.repaint();
            return;
        }

        if (validTargets.contains(-2) && (renderer.isTrayClicked(0, 0, color) || pointIndex == selectedFromPoint)) {
            int dv = bearingOffDie(selectedFromPoint, dice);
            SoundManager.getInstance().playPieceMove();
            client.sendMove(selectedFromPoint, -2, dv);
            waitingForServer = true;
            clearSelection();
            return;
        }

        if (validTargets.contains(pointIndex)) {
            int dv = (selectedFromPoint == -1) ? barEntryDie(pointIndex) : Math.abs(pointIndex - selectedFromPoint);
            SoundManager.getInstance().playPieceMove();
            client.sendMove(selectedFromPoint, pointIndex, dv);
            waitingForServer = true;
            clearSelection();
            return;
        }

        if (pointIndex == selectedFromPoint) {
            clearSelection();
            boardPanel.repaint();
            return;
        }

        clearSelection();
        if (pointIndex >= 0 && pointIndex < Board.POINT_COUNT) {
            Point p = board.getPoint(pointIndex);
            if (p != null && p.getOwner() == color && !p.isEmpty()) {
                selectedFromPoint = pointIndex;
                validTargets = calcTargets(pointIndex, dice);
                renderer.setSelectedPoint(selectedFromPoint);
                renderer.setHighlightedPoints(validTargets);
            }
        }
        boardPanel.repaint();
    }

    private int bearingOffDie(int from, Dice dice) {
        int dist = (localPlayer.getColor() == Player.WHITE) ? from + 1 : Board.POINT_COUNT - from;
        for (int d : dice.getRemainingMoves()) {
            if (d == dist || d > dist) {
                return d;
            }
        }
        return dice.getRemainingMoves().isEmpty() ? 0 : dice.getRemainingMoves().get(0);
    }

    private int barEntryDie(int target) {
        return (localPlayer.getColor() == Player.WHITE) ? Board.POINT_COUNT - target : target + 1;
    }

    private List<Integer> calcBarEntries(Dice dice) {
        List<Integer> targets = new ArrayList<>();
        if (dice == null || localPlayer == null) {
            return targets;
        }
        for (int d : dice.getRemainingMoves()) {
            int t = (localPlayer.getColor() == Player.WHITE) ? Board.POINT_COUNT - d : d - 1;
            if (t >= 0 && t < Board.POINT_COUNT) {
                Point p = gameState.getBoard().getPoint(t);
                if (p != null && p.isOpenFor(localPlayer.getColor()) && !targets.contains(t)) {
                    targets.add(t);
                }
            }
        }
        return targets;
    }

    private List<Integer> calcTargets(int from, Dice dice) {
        List<Integer> targets = new ArrayList<>();
        if (dice == null || localPlayer == null) {
            return targets;
        }
        Board board = gameState.getBoard();
        boolean bearOff = board.canBearOff(localPlayer);
        for (int d : dice.getRemainingMoves()) {
            int t = from + d * localPlayer.getDirection();
            if (t >= 0 && t < Board.POINT_COUNT) {
                Point p = board.getPoint(t);
                if (p != null && p.isOpenFor(localPlayer.getColor()) && !targets.contains(t)) {
                    targets.add(t);
                }
            } else if (bearOff && board.isValidMove(from, -2, dice, localPlayer) && !targets.contains(-2)) {
                targets.add(-2);
                break;
            }
        }
        return targets;
    }

    private void clearSelection() {
        selectedFromPoint = -2;
        selectedDieValue = -1;
        validTargets.clear();
        renderer.clearSelection();
    }

    private class BoardPanel extends JPanel {

        BoardPanel() {
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleClick(e.getX(), e.getY());
                }
            });
        }

        private void handleClick(int x, int y) {
            if (localPlayer == null || waitingForServer) {
                return;
            }
            if (localPlayer.hasBarPiece() && renderer.isBarClicked(x, y)) {
                onPointClicked(-1);
                return;
            }
            if (selectedFromPoint >= 0 && renderer.isTrayClicked(x, y, localPlayer.getColor()) && validTargets.contains(-2)) {
                int dv = bearingOffDie(selectedFromPoint, gameState.getDice());
                SoundManager.getInstance().playPieceMove();
                client.sendMove(selectedFromPoint, -2, dv);
                waitingForServer = true;
                clearSelection();
                boardPanel.repaint();
                return;
            }
            int pi = renderer.getPointIndexAt(x, y);
            if (pi >= 0) {
                onPointClicked(pi);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            if (gameState == null || gameState.getBoard() == null) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                g2.drawString("Oyun yükleniyor...", getWidth() / 2 - 90, getHeight() / 2);
                return;
            }
            renderer.render(g2, gameState.getBoard(), gameState.getDice(),
                    gameState.getCurrentPlayer(), gameState.getWaitingPlayer());
        }
    }

    private class PlayerCardPanel extends JPanel {

        private String title, username, colorStr = "";
        private int score;
        private final Color avatarBorderColor;
        private final boolean local;

        PlayerCardPanel(String title, String username, Color avatarBorderColor, boolean local) {
            this.title = title;
            this.username = username;
            this.avatarBorderColor = avatarBorderColor;
            this.local = local;
            setPreferredSize(new Dimension(220, 95));
            setMaximumSize(new Dimension(220, 95));
            setOpaque(false);
            setAlignmentX(CENTER_ALIGNMENT);
        }

        void updatePlayer(String u, int s) {
            username = u;
            score = s;
            repaint();
        }

        void updatePlayer(String u, int s, String cs) {
            username = u;
            score = s;
            colorStr = cs;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(95, 60, 30, 225), w, h, new Color(42, 24, 12, 225)));
            g2.fillRoundRect(0, 0, w, h, 18, 18);
            g2.setColor(new Color(155, 105, 60));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            int as = 42, ax = 10, ay = (h - as) / 2;
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(ax + 2, ay + 3, as, as);
            g2.setColor(avatarBorderColor);
            g2.fillOval(ax - 3, ay - 3, as + 6, as + 6);
            g2.setColor(local ? new Color(40, 150, 200) : new Color(70, 120, 180));
            g2.fillOval(ax, ay, as, as);
            drawFace(g2, ax, ay, as, local);

            int tx = ax + as + 11, lh = 18, sy = h / 2 - lh;
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(new Color(200, 175, 120));
            g2.drawString(title, tx, sy);
            String shown = (username == null ? "?" : username);
            if (shown.length() > 9) {
                shown = shown.substring(0, 9) + "..";
            }
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.setColor(new Color(245, 230, 200));
            g2.drawString(shown + (colorStr != null && !colorStr.isEmpty() ? " " + colorStr : ""), tx, sy + lh);
            g2.setColor(new Color(160, 215, 120));
            g2.drawString("Galibiyet: " + score, tx, sy + lh * 2);
            g2.dispose();
        }

        private void drawFace(Graphics2D g2, int x, int y, int sz, boolean local) {
            g2.setColor(new Color(255, 220, 185));
            g2.fillOval(x + 11, y + 10, 20, 22);
            g2.setColor(local ? new Color(45, 25, 20) : new Color(25, 20, 18));
            g2.fillArc(x + 9, y + 8, 24, 16, 0, 180);
            g2.setColor(Color.BLACK);
            g2.fillOval(x + 15, y + 19, 3, 3);
            g2.fillOval(x + 24, y + 19, 3, 3);
            if (local) {
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x + 13, y + 17, 8, 6);
                g2.drawOval(x + 22, y + 17, 8, 6);
                g2.drawLine(x + 21, y + 20, x + 22, y + 20);
            } else {
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawLine(x + 15, y + 27, x + 27, y + 27);
            }
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawArc(x + 17, y + 25, 9, 5, 180, 180);
        }
    }
}
