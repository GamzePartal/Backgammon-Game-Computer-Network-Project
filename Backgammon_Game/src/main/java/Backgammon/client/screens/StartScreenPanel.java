package Backgammon.client.screens;

import Backgammon.client.ScreenManager;
import Backgammon.client.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StartScreenPanel extends JPanel {

    private JTextField usernameField;
    private JTextField ipField;
    private JTextField portField;

    private JButton connectButton;
    private JButton exitButton;
    private JLabel statusLabel;

    private final ScreenManager screenManager;

    // Classpath'ten yükle — JAR içinde de çalışır
    private final Image backgroundImage = loadBackground();

    private Image loadBackground() {
        java.net.URL url = getClass().getResource("/images/arkaplanfoto.jpg");
        return (url != null) ? new ImageIcon(url).getImage() : null;
    }

    public StartScreenPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel contentPanel = new GlassPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
       contentPanel.setBorder(new EmptyBorder(35, 65, 35, 65));
        contentPanel.setPreferredSize(new Dimension(640, 520));

        JLabel titleLabel = createTitleLabel();

        JLabel subtitleLabel = new JLabel("Çok Oyunculu Tavla");
        subtitleLabel.setFont(new Font("Georgia", Font.ITALIC, 24));
        subtitleLabel.setForeground(new Color(255, 195, 70));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = createFormPanel();
        JPanel buttonPanel = createButtonPanel();

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        statusLabel.setForeground(new Color(245, 220, 160));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(35));
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(statusLabel);

        add(contentPanel);
    }

    private JLabel createTitleLabel() {
        JLabel label = new JLabel("TAVLA");
        label.setFont(new Font("Georgia", Font.BOLD, 58));
        label.setForeground(new Color(255, 190, 55));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 28, 18));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(500, 175));

        usernameField = createTextField("");          // Boş — placeholder gösterilecek
        addPlaceholder(usernameField, "İsminizi girin...");
        ipField = createTextField("127.0.0.1");
        portField = createTextField("5000");

        panel.add(createFieldLabel("Kullanıcı Adı:"));
        panel.add(usernameField);

        panel.add(createFieldLabel("Sunucu IP:"));
        panel.add(ipField);

        panel.add(createFieldLabel("Port:"));
        panel.add(portField);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 0));
        panel.setOpaque(false);

        connectButton = new RoundedButton("BAĞLAN",
                new Color(75, 150, 25),
                new Color(105, 185, 35));

        exitButton = new RoundedButton("ÇIKIŞ",
                new Color(175, 45, 40),
                new Color(215, 65, 55));

        connectButton.addActionListener(e -> {
            SoundManager.getInstance().playButtonClick();
            onConnectClicked();
        });
        exitButton.addActionListener(e -> {
            SoundManager.getInstance().playButtonClick();
            onExitClicked();
        });

        panel.add(connectButton);
        panel.add(exitButton);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(new Color(255, 235, 180));
        return label;
    }

    private JTextField createTextField(String text) {
        JTextField field = new RoundedTextField(text);
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setForeground(new Color(255, 245, 225));
        field.setCaretColor(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(8, 20, 8, 20));
        return field;
    }

    private void onConnectClicked() {
        if (!validateInputs()) return;

        String username = usernameField.getText().trim();
        // Placeholder metni yanlışlıkla geçtiyse engelle (validateInputs zaten yakalar ama güvenlik için)
        if (username.equals("İsminizi girin...")) {
            showStatus("Lütfen bir kullanıcı adı girin!", Color.RED);
            return;
        }

        String ip = ipField.getText().trim();
        int port;

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            showStatus("Geçersiz port numarası!", Color.RED);
            return;
        }

        connectButton.setEnabled(false);
        showStatus("Sunucuya bağlanılıyor...", new Color(255, 220, 120));

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return screenManager.connectToServer(ip, port, username);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();

                    if (success) {
                        showStatus("Bağlandı! Rakip bekleniyor...",
                                new Color(130, 240, 130));
                    } else {
                        showStatus("Bağlantı başarısız! IP ve port bilgilerini kontrol edin.",
                                Color.RED);
                        connectButton.setEnabled(true);
                    }

                } catch (Exception ex) {
                    showStatus("Hata: " + ex.getMessage(), Color.RED);
                    connectButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void onExitClicked() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Oyundan çıkmak istiyor musunuz?",
                "Çıkış",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private boolean validateInputs() {
        String username = usernameField.getText().trim();

        if (username.isEmpty() || username.equals("İsminizi girin...")) {
            showStatus("Kullanıcı adı boş bırakılamaz!", Color.RED);
            usernameField.requestFocus();
            return false;
        }

        if (username.length() < 2) {
            showStatus("Kullanıcı adı en az 2 karakter olmalıdır!", Color.RED);
            usernameField.requestFocus();
            return false;
        }

        if (username.length() > 16) {
            showStatus("Kullanıcı adı en fazla 16 karakter olabilir!", Color.RED);
            usernameField.requestFocus();
            return false;
        }

        if (ipField.getText().trim().isEmpty()) {
            showStatus("IP adresi boş bırakılamaz!", Color.RED);
            return false;
        }

        if (portField.getText().trim().isEmpty()) {
            showStatus("Port numarası boş bırakılamaz!", Color.RED);
            return false;
        }

        return true;
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public void showWaitingMessage() {
        showStatus("Rakip bekleniyor... Lütfen bekleyin.",
                new Color(255, 220, 120));
    }

  
    private void addPlaceholder(JTextField field, String placeholder) {
        final Color placeholderColor = new Color(180, 160, 130, 180);
        final Color activeColor      = new Color(255, 245, 225);

        field.setText(placeholder);
        field.setForeground(placeholderColor);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(activeColor);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(placeholderColor);
                }
            }
        });
    }

    public void reset() {
        connectButton.setEnabled(true);
        showStatus(" ", Color.WHITE);
        // Kullanıcı adı alanını placeholder'a döndür
        final Color placeholderColor = new Color(180, 160, 130, 180);
        usernameField.setText("İsminizi girin...");
        usernameField.setForeground(placeholderColor);
    }

    private static class GlassPanel extends JPanel {

        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 28;

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, arc, arc);

            g2.setColor(new Color(60, 30, 12, 135));
            g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, arc, arc);

            g2.setColor(new Color(210, 150, 45, 210));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, 0, getWidth() - 12, getHeight() - 12, arc, arc);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private static class RoundedTextField extends JTextField {

        public RoundedTextField(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(60, 35, 20, 120));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(190, 130, 45, 210));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {

        private final Color normalColor;
        private final Color hoverColor;
        private Color currentColor;

        public RoundedButton(String text, Color normalColor, Color hoverColor) {
            super(text);

            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.currentColor = normalColor;

            setFont(new Font("Arial", Font.BOLD, 21));
            setForeground(new Color(255, 240, 210));
            setPreferredSize(new Dimension(210, 62));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentColor = hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    currentColor = normalColor;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 28;

            int buttonWidth = getWidth() - 12;
            int buttonHeight = getHeight() - 14;

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(7, 8, getWidth() - 14, getHeight() - 12, arc, arc);

            g2.setColor(currentColor);
            g2.fillRoundRect(0, 0, buttonWidth, buttonHeight, arc, arc);

            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillRoundRect(0, 0, buttonWidth, buttonHeight / 2, arc, arc);

            g2.setFont(getFont());
            g2.setColor(getForeground());

            FontMetrics fm = g2.getFontMetrics();
            String text = getText();

            int textX = (buttonWidth - fm.stringWidth(text)) / 2;
            int textY = (buttonHeight - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(text, textX, textY);

            g2.dispose();
        }
    }
}