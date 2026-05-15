package Backgammon.client.screens;
import Backgammon.client.ScreenManager;
import Backgammon.client.SoundManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

//kullanıcı adı IP ve port bilgileri buradan alınır
//bağlantı işlemi SwingWorker ile arka planda yapılır
public class StartScreenPanel extends JPanel {

    private JTextField usernameField;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JLabel statusLabel;

    private final ScreenManager screenManager;
    private final Image backgroundImage;

    public StartScreenPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        java.net.URL url = getClass().getResource("/images/arkaplanfoto.jpg");
        backgroundImage = (url != null) ? new ImageIcon(url).getImage() : null;
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

        JPanel content = new GlassPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(35, 65, 35, 65));
        content.setPreferredSize(new Dimension(640, 520));

        JLabel title = styledLabel("TAVLA", new Font("Georgia", Font.BOLD, 58), new Color(255, 190, 55));
        JLabel subtitle = styledLabel("Çok Oyunculu Tavla", new Font("Georgia", Font.ITALIC, 24), new Color(255, 195, 70));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        statusLabel.setForeground(new Color(245, 220, 160));
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(35));
        content.add(buildFormPanel());
        content.add(Box.createVerticalStrut(30));
        content.add(buildButtonPanel());
        content.add(Box.createVerticalStrut(12));
        content.add(statusLabel);
        add(content);
    }

    private JLabel styledLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 28, 18));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(500, 175));

        usernameField = createField("");
        addPlaceholder(usernameField, "İsminizi girin...");
        ipField = createField("13.61.0.243");
        portField = createField("5000");

        panel.add(fieldLabel("Kullanıcı Adı:"));
        panel.add(usernameField);
        panel.add(fieldLabel("Sunucu IP:"));
        panel.add(ipField);
        panel.add(fieldLabel("Port:"));
        panel.add(portField);
        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 0));
        panel.setOpaque(false);

        connectButton = new RoundedButton("BAĞLAN", new Color(75, 150, 25), new Color(105, 185, 35));
        JButton exit = new RoundedButton("ÇIKIŞ", new Color(175, 45, 40), new Color(215, 65, 55));

        connectButton.addActionListener(e -> {
            SoundManager.getInstance().playButtonClick();
            onConnect();
        });
        exit.addActionListener(e -> {
            SoundManager.getInstance().playButtonClick();
            if (JOptionPane.showConfirmDialog(this, "Oyundan çıkmak istiyor musunuz?",
                    "Çıkış", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        panel.add(connectButton);
        panel.add(exit);
        return panel;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 18));
        l.setForeground(new Color(255, 235, 180));
        return l;
    }

    private JTextField createField(String text) {
        JTextField f = new RoundedTextField(text);
        f.setFont(new Font("Arial", Font.PLAIN, 18));
        f.setForeground(new Color(255, 245, 225));
        f.setCaretColor(Color.WHITE);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(8, 20, 8, 20));
        return f;
    }

    //Kullanıcı girişlerini kontrol eder ve servera bağlanma işlemini başlatır
    private void onConnect() {
        String username = usernameField.getText().trim();
        if (username.isEmpty() || username.equals("İsminizi girin...")) {
            showStatus("Kullanıcı adı boş bırakılamaz!", Color.RED);
            return;
        }
        if (username.length() < 2) {
            showStatus("Kullanıcı adı en az 2 karakter olmalıdır!", Color.RED);
            return;
        }
        if (username.length() > 16) {
            showStatus("Kullanıcı adı en fazla 16 karakter olabilir!", Color.RED);
            return;
        }
        if (ipField.getText().trim().isEmpty()) {
            showStatus("IP adresi boş bırakılamaz!", Color.RED);
            return;
        }
        if (portField.getText().trim().isEmpty()) {
            showStatus("Port numarası boş bırakılamaz!", Color.RED);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            showStatus("Geçersiz port numarası!", Color.RED);
            return;
        }

        String ip = ipField.getText().trim();
        connectButton.setEnabled(false);
        showStatus("Sunucuya bağlanılıyor...", new Color(255, 220, 120));

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return screenManager.connectToServer(ip, port, username);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        showStatus("Bağlandı! Rakip bekleniyor...", new Color(130, 240, 130));
                    } else {
                        showStatus("Bağlantı başarısız! IP ve port bilgilerini kontrol edin.", Color.RED);
                        connectButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    showStatus("Hata: " + ex.getMessage(), Color.RED);
                    connectButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void addPlaceholder(JTextField field, String placeholder) {
        final Color ph = new Color(180, 160, 130, 180), act = new Color(255, 245, 225);
        field.setText(placeholder);
        field.setForeground(ph);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(act);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(ph);
                }
            }
        });
    }

    private void showStatus(String msg, Color c) {
        statusLabel.setText(msg);
        statusLabel.setForeground(c);
    }

    public void showWaitingMessage() {
        showStatus("Rakip bekleniyor... Lütfen bekleyin.", new Color(255, 220, 120));
    }

    //başlangıç ekranının ilk haline döndürür
    public void reset() {
        connectButton.setEnabled(true);
        showStatus(" ", Color.WHITE);
        usernameField.setText("İsminizi girin...");
        usernameField.setForeground(new Color(180, 160, 130, 180));
    }

    private static class GlassPanel extends JPanel {

        GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        RoundedTextField(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        private final Color normal, hover;
        private Color current;

        RoundedButton(String text, Color normal, Color hover) {
            super(text);
            this.normal = normal;
            this.hover = hover;
            this.current = normal;
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
                    current = hover;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    current = normal;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 28, bw = getWidth() - 12, bh = getHeight() - 14;
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(7, 8, getWidth() - 14, getHeight() - 12, arc, arc);
            g2.setColor(current);
            g2.fillRoundRect(0, 0, bw, bh, arc, arc);
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillRoundRect(0, 0, bw, bh / 2, arc, arc);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (bw - fm.stringWidth(getText())) / 2, (bh - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
