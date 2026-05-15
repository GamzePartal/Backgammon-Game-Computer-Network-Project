package Backgammon.client;

import javax.swing.*;
import java.awt.*;

//Uygulamayı başlatır ana pencereyi oluşturur ScreenManager nesnesini kurar ve başlangıç ekranını gösterir
//arka plan müziğini başlatır
public class MainApp {

    private static final String TITLE = "Tavla - Cok Oyunculu";
    private static final int WIDTH = 1080;
    private static final int HEIGHT = 660;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Look&Feel yuklenemedi: " + e.getMessage());
            }

            JFrame frame = new JFrame(TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(WIDTH, HEIGHT);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);

            ScreenManager sm = new ScreenManager(frame);
            frame.setContentPane(sm.getMainPanel());
            frame.setVisible(true);
            SoundManager.getInstance().playBackground();
        });
    }
}
