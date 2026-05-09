package Backgammon.client;
import javax.swing.*;
import java.awt.*;
 


public class MainApp {
    // Uygulama penceresi başlığı
    private static final String WINDOW_TITLE  = "Tavla - Çok Oyunculu";
 
    // Pencere genişliği (piksel): tahta (820) + sağ panel (220) + kenar boşlukları
    private static final int    WINDOW_WIDTH  = 1080;
 
    // Pencere yüksekliği (piksel)
    private static final int    WINDOW_HEIGHT = 660;
 
    public static void main(String[] args) {
        // Tüm Swing bileşenleri EDT üzerinde oluşturulmalıdır
        SwingUtilities.invokeLater(() -> {
            try {
                // İşletim sisteminin yerel görünümünü kullan (Windows/macOS/Linux)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Sistem görünümü yüklenemezse varsayılan Swing görünümü kullanılır
                System.err.println("Look&Feel yüklenemedi: " + e.getMessage());
            }
 
            // Ana pencereyi oluştur
            JFrame frame = new JFrame(WINDOW_TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null); // Ekranın ortasına konumlandır
            frame.setResizable(true);
 
            // Ekran yöneticisini oluştur ve ana paneli frame'e ekle
            ScreenManager screenManager = new ScreenManager(frame);
            frame.setContentPane(screenManager.getMainPanel());
 
            // Pencereyi görünür yap
            frame.setVisible(true);
 
            System.out.println("[UYGULAMA] Tavla istemcisi başlatıldı.");
        });
    }
}