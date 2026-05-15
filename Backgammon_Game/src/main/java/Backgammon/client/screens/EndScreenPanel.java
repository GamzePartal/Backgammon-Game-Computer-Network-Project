package Backgammon.client.screens;

import Backgammon.client.ScreenManager;
import Backgammon.client.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

//oyun bittikten sonra sonuç ekranını gösterir
//Kazanan, kaybeden, mars bilgisi ve tekrar oynama seçeneği burada yer alır
public class EndScreenPanel extends JPanel {

    private final ScreenManager screenManager;
    private final Image backgroundImage;

    private JLabel titleLabel, resultLabel, marsLabel, winnerLabel, statusLabel;
    private JButton playAgainButton;

    public EndScreenPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        java.net.URL url = getClass().getResource("/images/arkaplanfoto.jpg");
        backgroundImage = (url != null) ? new ImageIcon(url).getImage() : null;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(10, 5, 2, 80));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel content = new RoundedPanel(28);
        content.setOpaque(false);
        content.setBackgroundColor(new Color(111, 78, 55, 180));
        content.setBorderColor(new Color(111, 78, 55, 180));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(40, 90, 40, 90));
        content.setPreferredSize(new Dimension(590, 480));

        titleLabel = centeredLabel("OYUN BİTTİ", new Font("Georgia", Font.BOLD, 40), new Color(245, 230, 210));
        resultLabel = centeredLabel(" ", new Font("Georgia", Font.BOLD, 30), new Color(100, 255, 100));
        marsLabel = centeredLabel(" ", new Font("Georgia", Font.BOLD, 22), new Color(255, 180, 50));
        winnerLabel = centeredLabel(" ", new Font("Arial", Font.BOLD, 19), new Color(245, 230, 210));
        statusLabel = centeredLabel(" ", new Font("Arial", Font.ITALIC, 14), new Color(230, 200, 170));

        playAgainButton = styledButton("Tekrar Oyna", new Color(35, 145, 65), new Color(25, 115, 50), new Color(55, 170, 85));
        playAgainButton.addActionListener(e -> {
            SoundManager.getInstance().playButtonClick();
            playAgainButton.setEnabled(false);
            statusLabel.setText("Rakip bekleniyor...");
            screenManager.requestRematch();
        });

        JButton menuButton = styledButton("Ana Menü", new Color(185, 55, 45), new Color(145, 35, 30), new Color(210, 70, 60));
        menuButton.addActionListener(e -> {
            SoundManager.getInstance().playButtonClick();
            screenManager.showStartScreen();
        });

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(playAgainButton);
        buttons.add(Box.createHorizontalStrut(20));
        buttons.add(menuButton);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(20));
        content.add(resultLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(marsLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(winnerLabel);
        content.add(Box.createVerticalStrut(16));
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(30));
        content.add(buttons);
        add(content);
    }

    private JLabel centeredLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    //Oyun sonucuna göre kazandın/kaybettin/mars mesajlarını ekrana yazar
    public void setResult(String winnerName, boolean isLocalWinner, boolean isMars, int winnerWins) {
        playAgainButton.setEnabled(true);
        statusLabel.setText(" ");
        titleLabel.setText(isMars ? "MARS!" : "OYUN BİTTİ");
        if (isLocalWinner) {
            resultLabel.setText("Tebrikler, Kazandın!");
            resultLabel.setForeground(isMars ? new Color(255, 200, 50) : new Color(100, 255, 100));
            marsLabel.setText(isMars ? " Mars! +2 Galibiyet" : " ");
        } else {
            resultLabel.setText(isMars ? "Mars oldun!" : "Kaybettin");
            resultLabel.setForeground(new Color(255, 100, 100));
            marsLabel.setText(isMars ? "Rakip +2 galibiyet aldı" : " ");
        }
        winnerLabel.setText(" KAZANAN : " + winnerName.toUpperCase());
    }

    public void showWaitingForRematch() {
        playAgainButton.setEnabled(false);
        statusLabel.setText("Rakip bekleniyor...");
    }

    public void showWaitingForOpponent() {
        statusLabel.setText("Rakip tekrar oynamak istiyor!");
    }

    private JButton styledButton(String text, Color normal, Color border, Color hover) {
        RoundedButton btn = new RoundedButton(text, 20);
        btn.setFont(new Font("Arial", Font.BOLD, 17));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(260, 50));
        btn.setPreferredSize(new Dimension(260, 50));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setButtonColor(normal);
        btn.setBorderColor(border);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setButtonColor(hover);
                btn.setFont(new Font("Arial", Font.BOLD, 18));
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setButtonColor(normal);
                btn.setFont(new Font("Arial", Font.BOLD, 17));
                btn.repaint();
            }
        });
        return btn;
    }

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private Color bg, border;

        RoundedPanel(int r) {
            this.radius = r;
            setOpaque(false);
        }

        void setBackgroundColor(Color c) {
            bg = c;
        }

        void setBorderColor(Color c) {
            border = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, radius, radius);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, radius, radius);
            g2.setStroke(new BasicStroke(3));
            g2.setColor(border);
            g2.drawRoundRect(1, 1, getWidth() - 15, getHeight() - 15, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {

        private final int radius;
        private Color btnColor, borderColor;

        RoundedButton(String text, int r) {
            super(text);
            this.radius = r;
            setOpaque(false);
        }

        void setButtonColor(Color c) {
            btnColor = c;
        }

        void setBorderColor(Color c) {
            borderColor = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 95));
            g2.fillRoundRect(5, 6, getWidth() - 10, getHeight() - 10, radius, radius);
            g2.setColor(btnColor);
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, radius, radius);
            g2.setStroke(new BasicStroke(2));
            g2.setColor(borderColor);
            g2.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
