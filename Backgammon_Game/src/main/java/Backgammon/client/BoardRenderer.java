package Backgammon.client;
import Backgammon.common.Board;
import Backgammon.common.Dice;
import Backgammon.common.Player;
import Backgammon.common.Point;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class BoardRenderer {

    private static final int BOARD_X = 20;
    private static final int BOARD_Y = 20;

    private static final int POINT_WIDTH = 55;
    private static final int BAR_WIDTH = 40;

    private static final int BOARD_WIDTH = (POINT_WIDTH * 12) + BAR_WIDTH;
    private static final int BOARD_HEIGHT = 560;

    private static final int POINT_HEIGHT = 235;
    private static final int PIECE_DIAMETER = 42;

    // Sağ kutu (tray) sabitleri — hem çizim hem tıklama algılama burada
    private static final int TRAY_X      = BOARD_X + BOARD_WIDTH + 14;
    private static final int TRAY_WIDTH  = 50;
    private static final int TRAY_HEIGHT = 200;
    private static final int TRAY_TOP_Y  = BOARD_Y + 20;                         // siyah kutu
    private static final int TRAY_BOT_Y  = BOARD_Y + BOARD_HEIGHT - TRAY_HEIGHT - 20; // beyaz kutu

    private static final Color BOARD_BG = new Color(245, 235, 220);
    private static final Color BOARD_BORDER = new Color(125, 88, 55);

    private static final Color TRIANGLE_BLUE = new Color(35, 145, 190);
    private static final Color TRIANGLE_LIGHT = new Color(235, 220, 195);

    private static final Color PIECE_WHITE = new Color(250, 248, 240);
    private static final Color PIECE_BLACK = new Color(35, 28, 22);
    private static final Color SELECTED_PIECE_RED = new Color(210, 40, 35);

    private static final Color PIECE_BORDER = new Color(90, 65, 40);

    private static final Color HIGHLIGHT_FILL = new Color(255, 220, 60, 75);
    private static final Color HIGHLIGHT_BORDER = new Color(255, 200, 0);

    private static final Color BAR_COLOR = new Color(145, 110, 75);

    private static final Color DICE_BLUE_DARK = new Color(0, 85, 190);
    private static final Color DICE_WHITE_DOT = new Color(255, 240, 250);

    private List<Integer> highlightedPoints;
    private int selectedPoint = -1;

    public void render(Graphics2D g2d,
                       Board board,
                       Dice dice,
                       Player player1,
                       Player player2) {

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        drawBoard(g2d);
        drawPoints(g2d);
        drawBarArea(g2d);

        if (highlightedPoints != null && !highlightedPoints.isEmpty()) {
            drawHighlights(g2d);
        }

        drawPieces(g2d, board);
        drawBarPieces(g2d, player1, player2);
        drawBorneOffTray(g2d, player1, player2);

        if (dice != null && dice.isRolled()) {
            drawDice(g2d, dice);
        }

        drawPointNumbers(g2d);
    }

    public void drawBoard(Graphics2D g2d) {
        g2d.setColor(BOARD_BORDER);
        g2d.fillRoundRect(
                BOARD_X - 8,
                BOARD_Y - 8,
                BOARD_WIDTH + 16,
                BOARD_HEIGHT + 16,
                18,
                18
        );

        GradientPaint boardGradient = new GradientPaint(
                BOARD_X,
                BOARD_Y,
                new Color(255, 248, 235),
                BOARD_X,
                BOARD_Y + BOARD_HEIGHT,
                BOARD_BG
        );

        g2d.setPaint(boardGradient);
        g2d.fillRect(BOARD_X, BOARD_Y, BOARD_WIDTH, BOARD_HEIGHT);
    }

    public void drawPoints(Graphics2D g2d) {
        for (int i = 0; i < 24; i++) {
            drawTriangle(g2d, i, false);
        }
    }

    private void drawTriangle(Graphics2D g2d, int pointIndex, boolean highlighted) {
        Color color = (pointIndex % 2 == 0)
                ? TRIANGLE_LIGHT
                : TRIANGLE_BLUE;

        Polygon triangle = getTrianglePolygon(pointIndex);

        g2d.setColor(color);
        g2d.fillPolygon(triangle);

        g2d.setColor(new Color(120, 95, 70, 100));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawPolygon(triangle);

        if (highlighted) {
            g2d.setColor(HIGHLIGHT_FILL);
            g2d.fillPolygon(triangle);

            g2d.setColor(HIGHLIGHT_BORDER);
            g2d.setStroke(new BasicStroke(4f));
            g2d.drawPolygon(triangle);

            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawPolygon(triangle);
        }
    }

    private Polygon getTrianglePolygon(int pointIndex) {
        int[] xPoints;
        int[] yPoints;

        if (pointIndex < 12) {
            int xBase = calculateXBase(pointIndex, false);

            int bottom = BOARD_Y + BOARD_HEIGHT;
            int apex = BOARD_Y + BOARD_HEIGHT - POINT_HEIGHT;

            xPoints = new int[]{
                    xBase,
                    xBase + POINT_WIDTH,
                    xBase + POINT_WIDTH / 2
            };

            yPoints = new int[]{
                    bottom,
                    bottom,
                    apex
            };

        } else {
            int col = pointIndex - 12;
            int xBase = calculateXBase(col, true);

            int top = BOARD_Y;
            int apex = BOARD_Y + POINT_HEIGHT;

            xPoints = new int[]{
                    xBase,
                    xBase + POINT_WIDTH,
                    xBase + POINT_WIDTH / 2
            };

            yPoints = new int[]{
                    top,
                    top,
                    apex
            };
        }

        return new Polygon(xPoints, yPoints, 3);
    }

    private void drawHighlights(Graphics2D g2d) {
        for (int pointIndex : highlightedPoints) {
            drawTriangle(g2d, pointIndex, true);
        }
    }

    private void drawPointNumbers(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();

        for (int i = 0; i < 12; i++) {
            int xBaseBottom = calculateXBase(i, false);
            String bottomText = String.valueOf(i + 1);

            int bottomX = xBaseBottom + POINT_WIDTH / 2
                    - fm.stringWidth(bottomText) / 2;
            int bottomY = BOARD_Y + BOARD_HEIGHT - 8;

            drawNumberBackground(g2d, bottomX, bottomY, bottomText, fm);

            g2d.setColor(new Color(50, 35, 25));
            g2d.drawString(bottomText, bottomX, bottomY);

            int xBaseTop = calculateXBase(i, true);
            String topText = String.valueOf(i + 13);

            int topX = xBaseTop + POINT_WIDTH / 2
                    - fm.stringWidth(topText) / 2;
            int topY = BOARD_Y + 15;

            drawNumberBackground(g2d, topX, topY, topText, fm);

            g2d.setColor(new Color(50, 35, 25));
            g2d.drawString(topText, topX, topY);
        }
    }

    private void drawNumberBackground(Graphics2D g2d,
                                      int x,
                                      int y,
                                      String text,
                                      FontMetrics fm) {

        int paddingX = 4;
        int paddingY = 2;

        int boxX = x - paddingX;
        int boxY = y - fm.getAscent() - paddingY + 2;
        int boxW = fm.stringWidth(text) + paddingX * 2;
        int boxH = fm.getAscent() + paddingY * 2;

        g2d.setColor(new Color(255, 248, 235, 190));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
    }

    public void drawBarArea(Graphics2D g2d) {
        int barX = BOARD_X + 6 * POINT_WIDTH;

        GradientPaint barGradient = new GradientPaint(
                barX,
                BOARD_Y,
                new Color(165, 130, 90),
                barX + BAR_WIDTH,
                BOARD_Y,
                BAR_COLOR.darker()
        );

        g2d.setPaint(barGradient);
        g2d.fillRect(barX, BOARD_Y, BAR_WIDTH, BOARD_HEIGHT);

        g2d.setColor(new Color(95, 65, 40));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRect(barX, BOARD_Y, BAR_WIDTH, BOARD_HEIGHT);
    }

    public void drawPieces(Graphics2D g2d, Board board) {
        if (board == null) {
            return;
        }

        Point[] points = board.getPoints();

        for (int i = 0; i < Board.POINT_COUNT; i++) {
            Point p = points[i];

            if (p == null || p.isEmpty()) {
                continue;
            }

            int count = p.getCount();
            int selectedDrawIndex = Math.min(count - 1, 4);

            for (int j = 0; j < count; j++) {
                if (j > 4) {
                    continue;
                }

                Color pieceColor;

                if (i == selectedPoint && j == selectedDrawIndex) {
                    pieceColor = SELECTED_PIECE_RED;
                } else {
                    pieceColor = p.getOwner() == Player.WHITE
                            ? PIECE_WHITE
                            : PIECE_BLACK;
                }

                int[] pos = getPiecePosition(i, j);

                String countText = null;

                if (count > 5 && j == 4) {
                    countText = String.valueOf(count);
                }

                drawPiece(g2d, pos[0], pos[1], pieceColor, countText);
            }
        }
    }

   
    public void drawBarPieces(Graphics2D g2d, Player player1, Player player2) {
        if (player1 == null || player2 == null) return;

        int barX = BOARD_X + 6 * POINT_WIDTH;
        int pieceSize = 32;
        int centerX = barX + (BAR_WIDTH - pieceSize) / 2;
        int midY = BOARD_Y + BOARD_HEIGHT / 2;
        int spacing = pieceSize + 4;

        for (Player p : new Player[]{player1, player2}) {
            if (p == null) continue;
            int count = p.getPiecesOnBar();
            if (count <= 0) continue;

            Color pieceColor = (p.getColor() == Player.WHITE) ? PIECE_WHITE : PIECE_BLACK;
            boolean isWhite = (p.getColor() == Player.WHITE);

            int showCount = Math.min(count, 4);
            for (int i = 0; i < showCount; i++) {
                int py;
                if (isWhite) {
                    py = midY + 4 + i * spacing;
                } else {
                    py = midY - 4 - pieceSize - i * spacing;
                }
                String label = (i == showCount - 1 && count > 1)
                        ? String.valueOf(count) : null;
                drawBarPiece(g2d, centerX, py, pieceSize, pieceColor, label);
            }
        }
    }

    private void drawBarPiece(Graphics2D g2d, int x, int y, int size,
                               Color color, String label) {
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(x + 2, y + 2, size, size);

        GradientPaint gp = new GradientPaint(x, y, color.brighter(),
                x, y + size, color.darker());
        g2d.setPaint(gp);
        g2d.fillOval(x, y, size, size);

        g2d.setColor(PIECE_BORDER);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(x, y, size, size);

        if (label != null) {
            g2d.setColor(color.equals(PIECE_WHITE) ? Color.BLACK : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int tx = x + (size - fm.stringWidth(label)) / 2;
            int ty = y + (size + fm.getAscent()) / 2 - 3;
            g2d.drawString(label, tx, ty);
        }
    }

    
   
    public void drawBorneOffTray(Graphics2D g2d, Player player1, Player player2) {
        if (player1 == null || player2 == null) return;

        for (Player p : new Player[]{player1, player2}) {
            if (p == null) continue;
            boolean isWhite = (p.getColor() == Player.WHITE);
            Color pieceColor = isWhite ? PIECE_WHITE : PIECE_BLACK;
            int trayY = isWhite ? TRAY_BOT_Y : TRAY_TOP_Y;
            drawBorneOffBox(g2d, TRAY_X, trayY, TRAY_WIDTH, TRAY_HEIGHT,
                    pieceColor, p.getPiecesBorneOff(), isWhite);
        }
    }

    private void drawBorneOffBox(Graphics2D g2d,
                                  int x, int y, int width, int height,
                                  Color pieceColor, int count,
                                  boolean isWhite) {

        // Acik renkli arka plan
        Color bgColor = isWhite
                ? new Color(240, 225, 195, 220)   // krem/bej — beyaz taşlar için
                : new Color(195, 175, 145, 220);  // orta kahve — siyah taşlar için
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, width, height, 14, 14);

        // Kenarlık
        Color borderColor = count > 0
                ? new Color(80, 180, 80)
                : new Color(160, 130, 90);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(count > 0 ? 2.5f : 1.5f));
        g2d.drawRoundRect(x, y, width, height, 14, 14);

        int smallSize = 16;
        int simX = x + (width - smallSize) / 2;
        int padding = 8;

        // Taş simgeleri yukaridan asagi istifle
        int maxShow = Math.min(count, 8);
        int totalPiecesH = maxShow * smallSize + (maxShow > 0 ? (maxShow - 1) * 2 : 0);
        // Sayac icin alt kisimda yer birak
        int availH = height - padding * 2 - 22; // 22px sayac icin
        if (totalPiecesH > availH) {
            // Sigmazsa cakistir
            maxShow = Math.min(count, 5);
        }

        int simStartY = y + padding;
        for (int i = 0; i < maxShow; i++) {
            int simY = simStartY + i * (smallSize + 2);
            if (simY + smallSize > y + height - 26) break;
            drawSmallPiece(g2d, simX, simY, smallSize, pieceColor);
        }

        // Sayac — kutunun alt kismi
        String numStr = String.valueOf(count);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        Color numColor = count > 0
                ? new Color(30, 120, 30)
                : new Color(120, 100, 70);
        g2d.setColor(numColor);
        FontMetrics fm = g2d.getFontMetrics();
        int numX = x + (width - fm.stringWidth(numStr)) / 2;
        int numY = y + height - 8;
        g2d.drawString(numStr, numX, numY);
    }

    public void drawCapturedPieces(Graphics2D g2d, Player player1, Player player2) {
        drawBorneOffTray(g2d, player1, player2);
    }

    private void drawSmallPiece(Graphics2D g2d,
                                int x,
                                int y,
                                int size,
                                Color color) {

        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.fillOval(x + 3, y + 3, size, size);

        GradientPaint gradient = new GradientPaint(
                x,
                y,
                color.brighter(),
                x,
                y + size,
                color.darker()
        );

        g2d.setPaint(gradient);
        g2d.fillOval(x, y, size, size);

        g2d.setColor(new Color(90, 65, 40));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, size, size);

        g2d.setColor(new Color(255, 255, 255, 70));
        g2d.drawOval(x + 3, y + 3, size - 6, size - 6);
    }

    public void drawDice(Graphics2D g2d, Dice dice) {
        int diceSize = 38;
        int gap = 10;

        // Sağ beyaz alan: bar'ın sağından tahtanın sağ kenarına kadar
        int barRightX = BOARD_X + 6 * POINT_WIDTH + BAR_WIDTH;
        int rightAreaWidth = BOARD_X + BOARD_WIDTH - barRightX; // 6 * POINT_WIDTH = 330px
        int rightAreaCenterX = barRightX + rightAreaWidth / 2;
        int boardCenterY = BOARD_Y + BOARD_HEIGHT / 2;

        int remainingCount = dice.getRemainingMoves().size();
        int totalW = 2 * diceSize + gap;
        int startX = rightAreaCenterX - totalW / 2;
        int startY = boardCenterY - diceSize / 2;

        if (dice.isDoubles()) {
            boolean die1Active = remainingCount >= 1;
            boolean die2Active = remainingCount >= 2;

            drawSingleDie(g2d, startX, startY, diceSize, dice.getDie1(), die1Active);
            drawSingleDie(g2d, startX + diceSize + gap, startY, diceSize, dice.getDie2(), die2Active);

            if (remainingCount > 0) {
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.setColor(new Color(255, 230, 80));
                String remainText = "x" + remainingCount;
                FontMetrics fm = g2d.getFontMetrics();
                int tx = rightAreaCenterX - fm.stringWidth(remainText) / 2;
                int ty = startY + diceSize + 18;
                g2d.drawString(remainText, tx, ty);
            }
        } else {
            boolean die1Active = dice.canUse(dice.getDie1());
            boolean die2Active = dice.canUse(dice.getDie2());

            drawSingleDie(g2d, startX, startY, diceSize, dice.getDie1(), die1Active);
            drawSingleDie(g2d, startX + diceSize + gap, startY, diceSize, dice.getDie2(), die2Active);
        }
    }

    private void drawSingleDie(Graphics2D g2d,
                               int x,
                               int y,
                               int size,
                               int value,
                               boolean active) {

        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillRoundRect(x + 4, y + 5, size, size, 14, 14);

        GradientPaint diceGradient = new GradientPaint(
                x,
                y,
                active ? new Color(20, 185, 255) : new Color(95, 145, 170),
                x + size,
                y + size,
                active ? DICE_BLUE_DARK : new Color(70, 90, 105)
        );

        g2d.setPaint(diceGradient);
        g2d.fillRoundRect(x, y, size, size, 14, 14);

        g2d.setColor(new Color(0, 55, 145));
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.drawRoundRect(x, y, size, size, 14, 14);

        g2d.setColor(new Color(255, 255, 255, 90));
        g2d.fillOval(x + 6, y + 5, size / 2, size / 5);

        g2d.setColor(new Color(255, 255, 255, 55));
        g2d.setStroke(new BasicStroke(1.1f));
        g2d.drawRoundRect(x + 4, y + 4, size - 8, size - 8, 10, 10);

        g2d.setColor(DICE_WHITE_DOT);
        drawDiceDots(g2d, x, y, size, value);
    }

    private void drawDiceDots(Graphics2D g2d,
                              int x,
                              int y,
                              int size,
                              int value) {

        int dotSize = Math.max(5, size / 5);
        int pad = size / 4;
        int midX = x + size / 2;
        int midY = y + size / 2;

        int left = x + pad;
        int right = x + size - pad;
        int top = y + pad;
        int bottom = y + size - pad;

        int[][] positions = {
                {left, top},
                {right, top},
                {left, midY},
                {midX, midY},
                {right, midY},
                {left, bottom},
                {right, bottom}
        };

        int[][] dotLayout = {
                {},
                {3},
                {0, 6},
                {0, 3, 6},
                {0, 1, 5, 6},
                {0, 1, 3, 5, 6},
                {0, 1, 2, 4, 5, 6}
        };

        if (value >= 1 && value <= 6) {
            for (int index : dotLayout[value]) {
                int px = positions[index][0];
                int py = positions[index][1];

                g2d.fillOval(
                        px - dotSize / 2,
                        py - dotSize / 2,
                        dotSize,
                        dotSize
                );
            }
        }
    }

    private void drawPiece(Graphics2D g2d,
                           int x,
                           int y,
                           Color color,
                           String countText) {

        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(x + 4, y + 4, PIECE_DIAMETER, PIECE_DIAMETER);

        GradientPaint gradient = new GradientPaint(
                x,
                y,
                color.brighter(),
                x,
                y + PIECE_DIAMETER,
                color.darker()
        );

        g2d.setPaint(gradient);
        g2d.fill(new Ellipse2D.Double(
                x,
                y,
                PIECE_DIAMETER,
                PIECE_DIAMETER
        ));

        g2d.setColor(PIECE_BORDER);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(x, y, PIECE_DIAMETER, PIECE_DIAMETER);

        g2d.setColor(new Color(255, 255, 255, 75));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(
                x + 4,
                y + 4,
                PIECE_DIAMETER - 8,
                PIECE_DIAMETER - 8
        );

        if (countText != null) {
            g2d.setColor(
                    color.equals(PIECE_WHITE)
                            ? Color.BLACK
                            : Color.WHITE
            );

            g2d.setFont(new Font("Arial", Font.BOLD, 13));

            FontMetrics fm = g2d.getFontMetrics();

            int tx = x + (PIECE_DIAMETER - fm.stringWidth(countText)) / 2;
            int ty = y + (PIECE_DIAMETER + fm.getAscent()) / 2 - 3;

            g2d.drawString(countText, tx, ty);
        }
    }

    private int calculateXBase(int col, boolean isTop) {
        int barX = BOARD_X + 6 * POINT_WIDTH;

        if (isTop) {
            if (col < 6) {
                return BOARD_X + BOARD_WIDTH - (col + 1) * POINT_WIDTH;
            } else {
                return BOARD_X + (11 - col) * POINT_WIDTH;
            }
        } else {
            if (col < 6) {
                return BOARD_X + col * POINT_WIDTH;
            } else {
                return barX + BAR_WIDTH + (col - 6) * POINT_WIDTH;
            }
        }
    }

    private int[] getPiecePosition(int pointIndex, int stackIndex) {
        int[] center = getPointCenter(pointIndex);

        int offset = stackIndex * (PIECE_DIAMETER + 3);

        int x = center[0] - PIECE_DIAMETER / 2;
        int y;

        if (pointIndex < 12) {
            y = BOARD_Y + BOARD_HEIGHT
                    - PIECE_DIAMETER
                    - 25
                    - offset;
        } else {
            y = BOARD_Y + 25 + offset;
        }

        return new int[]{x, y};
    }

    public int[] getPointCenter(int pointIndex) {
        int xBase;
        int y;

        if (pointIndex < 12) {
            xBase = calculateXBase(pointIndex, false);
            y = BOARD_Y + BOARD_HEIGHT - POINT_HEIGHT / 2;
        } else {
            xBase = calculateXBase(pointIndex - 12, true);
            y = BOARD_Y + POINT_HEIGHT / 2;
        }

        return new int[]{
                xBase + POINT_WIDTH / 2,
                y
        };
    }

    public int getPointIndexAt(int mouseX, int mouseY) {
        for (int i = 0; i < Board.POINT_COUNT; i++) {
            int[] center = getPointCenter(i);
            int halfW = POINT_WIDTH / 2;

            if (mouseX >= center[0] - halfW
                    && mouseX <= center[0] + halfW) {

                if (i < 12) {
                    if (mouseY >= BOARD_Y + BOARD_HEIGHT - POINT_HEIGHT
                            && mouseY <= BOARD_Y + BOARD_HEIGHT) {
                        return i;
                    }
                } else {
                    if (mouseY >= BOARD_Y
                            && mouseY <= BOARD_Y + POINT_HEIGHT) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    public boolean isBarClicked(int mouseX, int mouseY) {
        int barX = BOARD_X + 6 * POINT_WIDTH;

        return mouseX >= barX
                && mouseX <= barX + BAR_WIDTH
                && mouseY >= BOARD_Y
                && mouseY <= BOARD_Y + BOARD_HEIGHT;
    }

    
    public boolean isTrayClicked(int mouseX, int mouseY, int playerColor) {
        int trayY = (playerColor == Player.WHITE) ? TRAY_BOT_Y : TRAY_TOP_Y;
        return mouseX >= TRAY_X
                && mouseX <= TRAY_X + TRAY_WIDTH
                && mouseY >= trayY
                && mouseY <= trayY + TRAY_HEIGHT;
    }

    public void setHighlightedPoints(List<Integer> points) {
        this.highlightedPoints = points;
    }

    public void setSelectedPoint(int index) {
        this.selectedPoint = index;
    }

    public void clearSelection() {
        this.selectedPoint = -1;
        this.highlightedPoints = null;
    }
}