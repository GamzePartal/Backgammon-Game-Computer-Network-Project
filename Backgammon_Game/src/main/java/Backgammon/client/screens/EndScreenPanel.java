package Backgammon.client.screens;

import Backgammon.client.ScreenManager;
import Backgammon.client.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EndScreenPanel extends JPanel {

    private final ScreenManager screenManager;

    private JLabel  titleLabel;
    private JLabel  resultLabel;
    private JLabel  winnerLabel;
    private JLabel  marsLabel;
    private JLabel  statusLabel;   // "Rakip bekleniyor..." mesajı
    private JButton playAgainButton;
    private JButton menuButton;

    private final Image backgroundImage;

    public EndScreenPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        // Classpath'ten yükle — JAR içinde de çalışır
        java.net.URL imgUrl = getClass().getResource("/images/arkaplanfoto.jpg");
        backgroundImage = (imgUrl != null) ? new ImageIcon(imgUrl).getImage() : null;
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

        RoundedPanel contentPanel = new RoundedPanel(28);
        contentPanel.setOpaque(false);
        Color coffeeTone = new Color(111, 78, 55, 180); // Kahve tonu, hafif transparan
        contentPanel.setBackgroundColor(coffeeTone);
        contentPanel.setBorderColor(coffeeTone);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(40, 90, 40, 90));
        contentPanel.setPreferredSize(new Dimension(590, 480));

        titleLabel = new JLabel("OYUN BİTTİ");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 40));
        titleLabel.setForeground(new Color(245, 230, 210));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Georgia", Font.BOLD, 30));
        resultLabel.setForeground(new Color(100, 255, 100));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mars etiketi — sadece mars olduğunda görünür
        marsLabel = new JLabel(" ");
        marsLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        marsLabel.setForeground(new Color(255, 180, 50));
        marsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        winnerLabel = new JLabel(" ");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 19));
        winnerLabel.setForeground(new Color(245, 230, 210));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Rematch bekleme mesajı
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(new Color(230, 200, 170));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        playAgainButton = createStyledButton(
                "Tekrar Oyna",
                new Color(35, 145, 65),
                new Color(25, 115, 50),
                new Color(55, 170, 85)
        );
        // Bağlantıyı kesmeden sunucuya rematch isteği gönder
        playAgainButton.addActionListener(e -> {
            SoundManager.getInstance().playButtonClick();
            playAgainButton.setEnabled(false);
            statusLabel.setText("Rakip bekleniyor...");
            screenManager.requestRematch();
        });

        menuButton = createStyledButton(
                "Ana Menü",
                new Color(185, 55, 45),
                new Color(145, 35, 30),
                new Color(210, 70, 60)
        );
        // Ana menü: bağlantıyı kes ve başa dön
        menuButton.addActionListener(e -> {
            SoundManager.getInstance().playButtonClick();
            screenManager.showStartScreen();
        });

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(resultLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(marsLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(winnerLabel);
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(statusLabel);
        contentPanel.add(Box.createVerticalStrut(30));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.add(playAgainButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(menuButton);

        contentPanel.add(buttonPanel);

        add(contentPanel);
    }

   
    public void setResult(String winnerName, boolean isLocalWinner,
                          boolean isMars, int winnerWins) {
        // Tekrar oyna butonunu sıfırla
        playAgainButton.setEnabled(true);
        statusLabel.setText(" ");

        if (isLocalWinner) {
            if (isMars) {
                resultLabel.setText("Tebrikler, MARS Kazandın!");
                resultLabel.setForeground(new Color(255, 200, 50));
                marsLabel.setText(" Mars! +2 Galibiyet");
                titleLabel.setText("MARS!");
            } else {
                resultLabel.setText("Tebrikler, Kazandın!");
                resultLabel.setForeground(new Color(100, 255, 100));
                marsLabel.setText(" ");
                titleLabel.setText("OYUN BİTTİ");
            }
        } else {
            if (isMars) {
                resultLabel.setText("Mars'a geldin!");
                resultLabel.setForeground(new Color(255, 100, 100));
                marsLabel.setText("Rakip +2 galibiyet aldı");
                titleLabel.setText("MARS!");
            } else {
                resultLabel.setText("Kaybettin.");
                resultLabel.setForeground(new Color(255, 100, 100));
                marsLabel.setText(" ");
                titleLabel.setText("OYUN BİTTİ");
            }
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

   
    private JButton createStyledButton(String text, Color normalColor,
                                       Color borderColor, Color hoverColor) {
        JButton button = new RoundedButton(text, 20);
        button.setFont(new Font("Arial", Font.BOLD, 17));
        button.setForeground(Color.WHITE);
        button.setBackground(normalColor);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(260, 50));
        button.setPreferredSize(new Dimension(260, 50));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ((RoundedButton) button).setButtonColor(normalColor);
        ((RoundedButton) button).setBorderColor(borderColor);

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                ((RoundedButton) button).setButtonColor(hoverColor);
                button.setFont(new Font("Arial", Font.BOLD, 18));
                button.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                ((RoundedButton) button).setButtonColor(normalColor);
                button.setFont(new Font("Arial", Font.BOLD, 17));
                button.repaint();
            }
        });
        return button;
    }

  
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color backgroundColor;
        private Color borderColor;

        public RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        public void setBackgroundColor(Color c) { this.backgroundColor = c; }
        public void setBorderColor(Color c)     { this.borderColor = c; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, radius, radius);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, radius, radius);
            g2.setStroke(new BasicStroke(3));
            g2.setColor(borderColor);
            g2.drawRoundRect(1, 1, getWidth() - 15, getHeight() - 15, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {
        private final int radius;
        private Color buttonColor;
        private Color borderColor;

        public RoundedButton(String text, int radius) { super(text); this.radius = radius; setOpaque(false); }
        public void setButtonColor(Color c) { this.buttonColor = c; }
        public void setBorderColor(Color c) { this.borderColor = c; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 95));
            g2.fillRoundRect(5, 6, getWidth() - 10, getHeight() - 10, radius, radius);
            g2.setColor(buttonColor);
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, radius, radius);
            g2.setStroke(new BasicStroke(2));
            g2.setColor(borderColor);
            g2.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}