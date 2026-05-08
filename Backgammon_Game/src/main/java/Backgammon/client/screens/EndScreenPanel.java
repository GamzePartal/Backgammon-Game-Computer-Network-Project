package Backgammon.client.screens;

import Backgammon.client.ScreenManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EndScreenPanel extends JPanel {

    private final ScreenManager screenManager;

    private JLabel resultLabel;
    private JLabel winnerLabel;
    private JButton playAgainButton;
    private JButton menuButton;

    private Image backgroundImage;

    public EndScreenPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        backgroundImage = new ImageIcon("src/images/arkaplanfoto.jpg").getImage();

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
        contentPanel.setBackgroundColor(new Color(225, 205, 170, 205));
        contentPanel.setBorderColor(new Color(205, 180, 140, 220));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(50, 90, 50, 90));
        contentPanel.setPreferredSize(new Dimension(590, 430));

        JLabel titleLabel = new JLabel("OYUN BITTI");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 40));
        titleLabel.setForeground(new Color(85, 45, 15));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Georgia", Font.BOLD, 32));
        resultLabel.setForeground(new Color(25, 130, 40));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        winnerLabel = new JLabel(" ");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 21));
        winnerLabel.setForeground(new Color(50, 25, 10));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        playAgainButton = createStyledButton(
                "Tekrar Oyna",
                new Color(35, 145, 65),
                new Color(25, 115, 50),
                new Color(55, 170, 85)
        );
        playAgainButton.addActionListener(e -> screenManager.showStartScreen());

        menuButton = createStyledButton(
                "Ana Menu",
                new Color(185, 55, 45),
                new Color(145, 35, 30),
                new Color(210, 70, 60)
        );
        menuButton.addActionListener(e -> screenManager.showStartScreen());

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(32));
        contentPanel.add(resultLabel);
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(winnerLabel);
        contentPanel.add(Box.createVerticalStrut(42));
        contentPanel.add(playAgainButton);
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(menuButton);

        add(contentPanel);
    }

    private JButton createStyledButton(String text, Color normalColor, Color borderColor, Color hoverColor) {
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
            @Override
            public void mouseEntered(MouseEvent e) {
                ((RoundedButton) button).setButtonColor(hoverColor);
                button.setFont(new Font("Arial", Font.BOLD, 18));
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((RoundedButton) button).setButtonColor(normalColor);
                button.setFont(new Font("Arial", Font.BOLD, 17));
                button.repaint();
            }
        });

        return button;
    }

    public void setResult(String winnerName, boolean isLocalWinner) {
        if (isLocalWinner) {
            resultLabel.setText("Tebrikler, Kazandin!");
            resultLabel.setForeground(new Color(20, 135, 40));
        } else {
            resultLabel.setText("Kaybettin.");
            resultLabel.setForeground(new Color(180, 40, 35));
        }

        winnerLabel.setText("Kazanan: " + winnerName);
    }

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private Color backgroundColor;
        private Color borderColor;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
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

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setOpaque(false);
        }

        public void setButtonColor(Color buttonColor) {
            this.buttonColor = buttonColor;
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
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