package Backgammon.client;

import Backgammon.common.Board;
import Backgammon.common.Dice;
import Backgammon.common.Player;
import Backgammon.common.Point;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

// bu class oyunun kurallarını yönetmez sadece görsel çizim ve koordinat hesaplama yapar
public class BoardRenderer {

    private static final int BOARD_X = 20, BOARD_Y = 20;
    private static final int POINT_WIDTH = 55, BAR_WIDTH = 40;
    private static final int BOARD_WIDTH = POINT_WIDTH * 12 + BAR_WIDTH;
    private static final int BOARD_HEIGHT = 560, POINT_HEIGHT = 235;
    private static final int PIECE_DIAM = 42;

    private static final int TRAY_X = BOARD_X + BOARD_WIDTH + 14;
    private static final int TRAY_WIDTH = 50, TRAY_HEIGHT = 200;
    private static final int TRAY_TOP_Y = BOARD_Y + 20;
    private static final int TRAY_BOT_Y = BOARD_Y + BOARD_HEIGHT - TRAY_HEIGHT - 20;

    private static final Color BOARD_BG = new Color(245, 235, 220);
    private static final Color BOARD_BORDER = new Color(125, 88, 55);
    private static final Color TRI_BLUE = new Color(35, 145, 190);
    private static final Color TRI_LIGHT = new Color(235, 220, 195);
    private static final Color PIECE_WHITE = new Color(250, 248, 240);
    private static final Color PIECE_BLACK = new Color(35, 28, 22);
    private static final Color PIECE_SELECTED = new Color(210, 40, 35);
    private static final Color PIECE_BORDER = new Color(90, 65, 40);
    private static final Color HL_FILL = new Color(255, 220, 60, 75);
    private static final Color HL_BORDER = new Color(255, 200, 0);
    private static final Color BAR_COLOR = new Color(145, 110, 75);
    private static final Color DICE_DARK = new Color(0, 85, 190);
    private static final Color DICE_DOT = new Color(255, 240, 250);

    private List<Integer> highlightedPoints; //zar atıldıktan sonra taşların gideceği haneleri tutar
    private int selectedPoint = -1;  //seçilen hane indexi başta -1 çünkü seçilen hane yok
    private int localPlayerColor = Player.WHITE; //oyuncunun rengini tutar

    public void setLocalPlayerColor(int color) {
        this.localPlayerColor = color;
    }

    //oyuncunun rengine göre tahtanın taşlarının yönleri belirlenir
    private int toDisplay(int idx) {
        return (localPlayerColor == Player.BLACK) ? (Board.POINT_COUNT - 1 - idx) : idx;
    }

    private int toBoard(int idx) {
        return toDisplay(idx);
    }

    //tahta çizilir
    public void render(Graphics2D g2d, Board board, Dice dice, Player p1, Player p2) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBoard(g2d);
        drawPoints(g2d);
        drawBarArea(g2d);
        if (highlightedPoints != null && !highlightedPoints.isEmpty()) {
            drawHighlights(g2d);
        }
        drawPieces(g2d, board);
        drawBarPieces(g2d, p1, p2);
        drawBorneOffTray(g2d, p1, p2);
        if (dice != null && dice.isRolled()) {
            drawDice(g2d, dice);
        }
        drawPointNumbers(g2d);
    }

    //Tavla tahtasının arka planını ve kenarlığını çizer
    public void drawBoard(Graphics2D g2d) {
        g2d.setColor(BOARD_BORDER);
        g2d.fillRoundRect(BOARD_X - 8, BOARD_Y - 8, BOARD_WIDTH + 16, BOARD_HEIGHT + 16, 18, 18);
        g2d.setPaint(new GradientPaint(BOARD_X, BOARD_Y, new Color(255, 248, 235), BOARD_X, BOARD_Y + BOARD_HEIGHT, BOARD_BG));
        g2d.fillRect(BOARD_X, BOARD_Y, BOARD_WIDTH, BOARD_HEIGHT);
    }

    //24 tavla hanesinin üçgenlerini çizer
    public void drawPoints(Graphics2D g2d) {
        for (int i = 0; i < 24; i++) {
            drawTriangle(g2d, i, false);
        }
    }

    //Tek bir üçgen haneyi çizer hl true ise vurgulu çizer
    private void drawTriangle(Graphics2D g2d, int di, boolean hl) {
        Color base = (di % 2 == 0) ? TRI_LIGHT : TRI_BLUE;
        Polygon tri = getTriangle(di);
        g2d.setColor(base);
        g2d.fillPolygon(tri);
        g2d.setColor(new Color(120, 95, 70, 100));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawPolygon(tri);
        if (hl) {
            g2d.setColor(HL_FILL);
            g2d.fillPolygon(tri);
            g2d.setColor(HL_BORDER);
            g2d.setStroke(new BasicStroke(4f));
            g2d.drawPolygon(tri);
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawPolygon(tri);
        }
    }

    //Belirtilen ekran indeksine göre üçgenin koordinatlarını hesaplar
    private Polygon getTriangle(int di) {
        int[] xp, yp;
        if (di < 12) {
            int xb = xBase(di, false), bot = BOARD_Y + BOARD_HEIGHT, apex = bot - POINT_HEIGHT;
            xp = new int[]{xb, xb + POINT_WIDTH, xb + POINT_WIDTH / 2};
            yp = new int[]{bot, bot, apex};
        } else {
            int xb = xBase(di - 12, true), top = BOARD_Y, apex = top + POINT_HEIGHT;
            xp = new int[]{xb, xb + POINT_WIDTH, xb + POINT_WIDTH / 2};
            yp = new int[]{top, top, apex};
        }
        return new Polygon(xp, yp, 3);
    }

    //Geçerli hamle hedeflerini vurgulu olarak çizer
    private void drawHighlights(Graphics2D g2d) {
        for (int bi : highlightedPoints) {
            if (bi >= 0) {
                drawTriangle(g2d, toDisplay(bi), true);
            }
        }
    }

    //Ortadaki bar alanını çizer
    public void drawBarArea(Graphics2D g2d) {
        int bx = BOARD_X + 6 * POINT_WIDTH;
        g2d.setPaint(new GradientPaint(bx, BOARD_Y, new Color(165, 130, 90), bx + BAR_WIDTH, BOARD_Y, BAR_COLOR.darker()));
        g2d.fillRect(bx, BOARD_Y, BAR_WIDTH, BOARD_HEIGHT);
        g2d.setColor(new Color(95, 65, 40));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRect(bx, BOARD_Y, BAR_WIDTH, BOARD_HEIGHT);
    }

    //Hanelerin numaralarını tahtaya yazar
    private void drawPointNumbers(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        for (int di = 0; di < 24; di++) {
            String lbl = String.valueOf(toBoard(di) + 1);
            int x = (di < 12 ? xBase(di, false) : xBase(di - 12, true)) + POINT_WIDTH / 2 - fm.stringWidth(lbl) / 2;
            int y = di < 12 ? BOARD_Y + BOARD_HEIGHT - 8 : BOARD_Y + 15;
            g2d.setColor(new Color(255, 248, 235, 190));
            g2d.fillRoundRect(x - 4, y - fm.getAscent(), fm.stringWidth(lbl) + 8, fm.getAscent() + 4, 8, 8);
            g2d.setColor(new Color(50, 35, 25));
            g2d.drawString(lbl, x, y);
        }
    }

    //Tahtadaki tüm taşları ilgili hanelere çizer
    public void drawPieces(Graphics2D g2d, Board board) {
        if (board == null) {
            return;
        }
        for (int bi = 0; bi < Board.POINT_COUNT; bi++) {
            Point p = board.getPoints()[bi];
            if (p == null || p.isEmpty()) {
                continue;
            }
            int count = p.getCount(), selIdx = Math.min(count - 1, 4);
            for (int j = 0; j <= Math.min(count - 1, 4); j++) {
                Color c = (bi == selectedPoint && j == selIdx) ? PIECE_SELECTED
                        : (p.getOwner() == Player.WHITE ? PIECE_WHITE : PIECE_BLACK);
                int[] pos = piecePos(toDisplay(bi), j);
                drawPiece(g2d, pos[0], pos[1], c, (count > 5 && j == 4) ? String.valueOf(count) : null);
            }
        }
    }

    //Kırılan ve barda bekleyen taşları çizer
    public void drawBarPieces(Graphics2D g2d, Player p1, Player p2) {
        if (p1 == null || p2 == null) {
            return;
        }
        int bx = BOARD_X + 6 * POINT_WIDTH, sz = 32, cx = bx + (BAR_WIDTH - sz) / 2;
        int midY = BOARD_Y + BOARD_HEIGHT / 2, sp = sz + 4;
        for (Player p : new Player[]{p1, p2}) {
            if (p == null || p.getPiecesOnBar() <= 0) {
                continue;
            }
            int cnt = p.getPiecesOnBar();
            boolean goDown = (p.getColor() == localPlayerColor);
            Color c = (p.getColor() == Player.WHITE) ? PIECE_WHITE : PIECE_BLACK;
            int show = Math.min(cnt, 4);
            for (int i = 0; i < show; i++) {
                int py = goDown ? midY + 4 + i * sp : midY - 4 - sz - i * sp;
                drawBarPiece(g2d, cx, py, sz, c, (i == show - 1 && cnt > 1) ? String.valueOf(cnt) : null);
            }
        }
    }

    //Oyuncuların topladığı taşları sağdaki bölmede gösterir
    public void drawBorneOffTray(Graphics2D g2d, Player p1, Player p2) {
        if (p1 == null || p2 == null) {
            return;
        }
        for (Player p : new Player[]{p1, p2}) {
            if (p == null) {
                continue;
            }
            Color c = (p.getColor() == Player.WHITE) ? PIECE_WHITE : PIECE_BLACK;
            drawBorneOffBox(g2d, TRAY_X, (p.getColor() == localPlayerColor) ? TRAY_BOT_Y : TRAY_TOP_Y,
                    TRAY_WIDTH, TRAY_HEIGHT, c, p.getPiecesBorneOff());
        }
    }

    //Toplanan taşları çizmek için drawBorneOffTray() metodunu çağırır
    public void drawCapturedPieces(Graphics2D g2d, Player p1, Player p2) {
        drawBorneOffTray(g2d, p1, p2);
    }

    //Zarları ve kalan zar kullanım durumunu çizer
    public void drawDice(Graphics2D g2d, Dice dice) {
        int sz = 38, gap = 10, bRX = BOARD_X + 6 * POINT_WIDTH + BAR_WIDTH;
        int cx = bRX + (BOARD_X + BOARD_WIDTH - bRX) / 2, sy = BOARD_Y + BOARD_HEIGHT / 2 - sz / 2, sx = cx - (2 * sz + gap) / 2;
        int rem = dice.getRemainingMoves().size();
        if (dice.isDoubles()) {
            drawSingleDie(g2d, sx, sy, sz, dice.getDie1(), rem >= 1);
            drawSingleDie(g2d, sx + sz + gap, sy, sz, dice.getDie2(), rem >= 2);
            if (rem > 0) {
                String rt = "x" + rem;
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.setColor(new Color(255, 230, 80));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(rt, cx - fm.stringWidth(rt) / 2, sy + sz + 18);
            }
        } else {
            drawSingleDie(g2d, sx, sy, sz, dice.getDie1(), dice.canUse(dice.getDie1()));
            drawSingleDie(g2d, sx + sz + gap, sy, sz, dice.getDie2(), dice.canUse(dice.getDie2()));
        }
    }

    private void drawSingleDie(Graphics2D g2d, int x, int y, int sz, int value, boolean active) {
        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillRoundRect(x + 4, y + 5, sz, sz, 14, 14);
        g2d.setPaint(new GradientPaint(x, y, active ? new Color(20, 185, 255) : new Color(95, 145, 170),
                x + sz, y + sz, active ? DICE_DARK : new Color(70, 90, 105)));
        g2d.fillRoundRect(x, y, sz, sz, 14, 14);
        g2d.setColor(new Color(0, 55, 145));
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.drawRoundRect(x, y, sz, sz, 14, 14);
        g2d.setColor(new Color(255, 255, 255, 90));
        g2d.fillOval(x + 6, y + 5, sz / 2, sz / 5);
        g2d.setColor(new Color(255, 255, 255, 55));
        g2d.setStroke(new BasicStroke(1.1f));
        g2d.drawRoundRect(x + 4, y + 4, sz - 8, sz - 8, 10, 10);
        g2d.setColor(DICE_DOT);
        drawDiceDots(g2d, x, y, sz, value);
    }

    //Zar üzerindeki noktaları zar değerine göre çizer
    private void drawDiceDots(Graphics2D g2d, int x, int y, int sz, int value) {
        int dot = Math.max(5, sz / 5), pad = sz / 4;
        int mx = x + sz / 2, my = y + sz / 2, l = x + pad, r = x + sz - pad, t = y + pad, b = y + sz - pad;
        int[][] pos = {{l, t}, {r, t}, {l, my}, {mx, my}, {r, my}, {l, b}, {r, b}};
        int[][] lay = {{}, {3}, {0, 6}, {0, 3, 6}, {0, 1, 5, 6}, {0, 1, 3, 5, 6}, {0, 1, 2, 4, 5, 6}};
        if (value >= 1 && value <= 6) {
            for (int i : lay[value]) {
                g2d.fillOval(pos[i][0] - dot / 2, pos[i][1] - dot / 2, dot, dot);
            }
        }
    }

    //Normal tavla taşını çizer. Eğer hanede 5’ten fazla taş varsa sayı gösterir
    private void drawPiece(Graphics2D g2d, int x, int y, Color c, String cnt) {
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(x + 4, y + 4, PIECE_DIAM, PIECE_DIAM);
        g2d.setPaint(new GradientPaint(x, y, c.brighter(), x, y + PIECE_DIAM, c.darker()));
        g2d.fill(new Ellipse2D.Double(x, y, PIECE_DIAM, PIECE_DIAM));
        g2d.setColor(PIECE_BORDER);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(x, y, PIECE_DIAM, PIECE_DIAM);
        g2d.setColor(new Color(255, 255, 255, 75));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(x + 4, y + 4, PIECE_DIAM - 8, PIECE_DIAM - 8);
        if (cnt != null) {
            g2d.setColor(c.equals(PIECE_WHITE) ? Color.BLACK : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(cnt, x + (PIECE_DIAM - fm.stringWidth(cnt)) / 2, y + (PIECE_DIAM + fm.getAscent()) / 2 - 3);
        }
    }

    //Bardaki küçük taşları çizer
    private void drawBarPiece(Graphics2D g2d, int x, int y, int sz, Color c, String lbl) {
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(x + 2, y + 2, sz, sz);
        g2d.setPaint(new GradientPaint(x, y, c.brighter(), x, y + sz, c.darker()));
        g2d.fillOval(x, y, sz, sz);
        g2d.setColor(PIECE_BORDER);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(x, y, sz, sz);
        if (lbl != null) {
            g2d.setColor(c.equals(PIECE_WHITE) ? Color.BLACK : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(lbl, x + (sz - fm.stringWidth(lbl)) / 2, y + (sz + fm.getAscent()) / 2 - 3);
        }
    }

    //Toplanan taş kutusunu ve toplanan taş sayısını çizer
    private void drawBorneOffBox(Graphics2D g2d, int x, int y, int w, int h, Color c, int cnt) {
        boolean white = c.equals(PIECE_WHITE);
        g2d.setColor(white ? new Color(240, 225, 195, 220) : new Color(195, 175, 145, 220));
        g2d.fillRoundRect(x, y, w, h, 14, 14);
        g2d.setColor(cnt > 0 ? new Color(80, 180, 80) : new Color(160, 130, 90));
        g2d.setStroke(new BasicStroke(cnt > 0 ? 2.5f : 1.5f));
        g2d.drawRoundRect(x, y, w, h, 14, 14);
        int ss = 16, sx = x + (w - ss) / 2, simY = y + 8;
        for (int i = 0; i < Math.min(cnt, 8); i++) {
            int iy = simY + i * (ss + 2);
            if (iy + ss > y + h - 26) {
                break;
            }
            drawSmallPiece(g2d, sx, iy, ss, c);
        }
        String num = String.valueOf(cnt);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(cnt > 0 ? new Color(30, 120, 30) : new Color(120, 100, 70));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(num, x + (w - fm.stringWidth(num)) / 2, y + h - 8);
    }

    private void drawSmallPiece(Graphics2D g2d, int x, int y, int sz, Color c) {
        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.fillOval(x + 3, y + 3, sz, sz);
        g2d.setPaint(new GradientPaint(x, y, c.brighter(), x, y + sz, c.darker()));
        g2d.fillOval(x, y, sz, sz);
        g2d.setColor(new Color(90, 65, 40));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, sz, sz);
        g2d.setColor(new Color(255, 255, 255, 70));
        g2d.drawOval(x + 3, y + 3, sz - 6, sz - 6);
    }

    //Hanenin yatay başlangıç koordinatını hesaplar.
    private int xBase(int col, boolean isTop) {
        int bx = BOARD_X + 6 * POINT_WIDTH;
        if (isTop) {
            return col < 6 ? BOARD_X + BOARD_WIDTH - (col + 1) * POINT_WIDTH : BOARD_X + (11 - col) * POINT_WIDTH;
        } else {
            return col < 6 ? BOARD_X + col * POINT_WIDTH : bx + BAR_WIDTH + (col - 6) * POINT_WIDTH;
        }
    }

    //Bir taşın ekrandaki x-y konumunu hesaplar
    private int[] piecePos(int di, int si) {
        int[] c = pointCenter(di), off = new int[]{c[0] - PIECE_DIAM / 2,
            di < 12 ? BOARD_Y + BOARD_HEIGHT - PIECE_DIAM - 25 - si * (PIECE_DIAM + 3) : BOARD_Y + 25 + si * (PIECE_DIAM + 3)};
        return off;
    }

    //Bir hanenin merkez koordinatını hesaplar
    private int[] pointCenter(int di) {
        int xb = di < 12 ? xBase(di, false) : xBase(di - 12, true);
        int y = di < 12 ? BOARD_Y + BOARD_HEIGHT - POINT_HEIGHT / 2 : BOARD_Y + POINT_HEIGHT / 2;
        return new int[]{xb + POINT_WIDTH / 2, y};
    }

    public int[] getPointCenter(int boardIndex) {
        return pointCenter(toDisplay(boardIndex));
    }

    public int getPointIndexAt(int mx, int my) {
        for (int di = 0; di < Board.POINT_COUNT; di++) {
            int[] c = pointCenter(di);
            int hw = POINT_WIDTH / 2;
            if (mx < c[0] - hw || mx > c[0] + hw) {
                continue;
            }
            if (di < 12 && my >= BOARD_Y + BOARD_HEIGHT - POINT_HEIGHT && my <= BOARD_Y + BOARD_HEIGHT) {
                return toBoard(di);
            }
            if (di >= 12 && my >= BOARD_Y && my <= BOARD_Y + POINT_HEIGHT) {
                return toBoard(di);
            }
        }
        return -1;
    }

    public boolean isBarClicked(int mx, int my) {
        int bx = BOARD_X + 6 * POINT_WIDTH;
        return mx >= bx && mx <= bx + BAR_WIDTH && my >= BOARD_Y && my <= BOARD_Y + BOARD_HEIGHT;
    }

    //Mouse tıklamasının taş toplama alanına denk gelip gelmediğini kontrol eder
    public boolean isTrayClicked(int mx, int my, int playerColor) {
        int ty = (playerColor == localPlayerColor) ? TRAY_BOT_Y : TRAY_TOP_Y;
        return mx >= TRAY_X && mx <= TRAY_X + TRAY_WIDTH && my >= ty && my <= ty + TRAY_HEIGHT;
    }

    public void setHighlightedPoints(List<Integer> pts) {
        this.highlightedPoints = pts;
    }

    public void setSelectedPoint(int boardIndex) {
        this.selectedPoint = boardIndex;
    }

    public void clearSelection() {
        selectedPoint = -1;
        highlightedPoints = null;
    }
}
