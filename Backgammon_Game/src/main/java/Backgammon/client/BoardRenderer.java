package Backgammon.client;

import Backgammon.common.Board;
import Backgammon.common.Dice;
import Backgammon.common.Player;
import Backgammon.common.Point;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;


public class BoardRenderer {

  
    private static final int BOARD_X        = 20;
    private static final int BOARD_Y        = 20;
    private static final int POINT_WIDTH    = 55;
    private static final int BAR_WIDTH      = 40;
    private static final int BOARD_WIDTH    = (POINT_WIDTH * 12) + BAR_WIDTH;
    private static final int BOARD_HEIGHT   = 560;
    private static final int POINT_HEIGHT   = 235;
    private static final int PIECE_DIAMETER = 42;

    // Tray (taş toplama kutusu) — her zaman tahtanın SAĞINDA sabit
    private static final int TRAY_X      = BOARD_X + BOARD_WIDTH + 14;
    private static final int TRAY_WIDTH  = 50;
    private static final int TRAY_HEIGHT = 200;
    private static final int TRAY_TOP_Y  = BOARD_Y + 20;
    private static final int TRAY_BOT_Y  = BOARD_Y + BOARD_HEIGHT - TRAY_HEIGHT - 20;

   
    private static final Color BOARD_BG           = new Color(245, 235, 220);
    private static final Color BOARD_BORDER       = new Color(125, 88, 55);
    private static final Color TRIANGLE_BLUE      = new Color(35, 145, 190);
    private static final Color TRIANGLE_LIGHT     = new Color(235, 220, 195);
    private static final Color PIECE_WHITE        = new Color(250, 248, 240);
    private static final Color PIECE_BLACK        = new Color(35, 28, 22);
    private static final Color SELECTED_PIECE_RED = new Color(210, 40, 35);
    private static final Color PIECE_BORDER       = new Color(90, 65, 40);
    private static final Color HIGHLIGHT_FILL     = new Color(255, 220, 60, 75);
    private static final Color HIGHLIGHT_BORDER   = new Color(255, 200, 0);
    private static final Color BAR_COLOR          = new Color(145, 110, 75);
    private static final Color DICE_BLUE_DARK     = new Color(0, 85, 190);
    private static final Color DICE_WHITE_DOT     = new Color(255, 240, 250);

 
    private List<Integer> highlightedPoints;
    private int selectedPoint = -1;


    private int localPlayerColor = Player.WHITE;

    public void setLocalPlayerColor(int color) {
        this.localPlayerColor = color;
    }

  
    private int toDisplay(int boardIndex) {
        return (localPlayerColor == Player.BLACK)
                ? (Board.POINT_COUNT - 1 - boardIndex)
                : boardIndex;
    }

    /** Display → board (simetrik, aynı formül). */
    private int toBoard(int displayIndex) {
        return toDisplay(displayIndex);
    }


    public void render(Graphics2D g2d,
                       Board board,
                       Dice dice,
                       Player player1,
                       Player player2) {

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

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
        g2d.fillRoundRect(BOARD_X - 8, BOARD_Y - 8,
                          BOARD_WIDTH + 16, BOARD_HEIGHT + 16, 18, 18);
        GradientPaint gp = new GradientPaint(
                BOARD_X, BOARD_Y, new Color(255, 248, 235),
                BOARD_X, BOARD_Y + BOARD_HEIGHT, BOARD_BG);
        g2d.setPaint(gp);
        g2d.fillRect(BOARD_X, BOARD_Y, BOARD_WIDTH, BOARD_HEIGHT);
    }

  

    public void drawPoints(Graphics2D g2d) {
        for (int displayIndex = 0; displayIndex < 24; displayIndex++) {
            drawTriangleByDisplay(g2d, displayIndex, false);
        }
    }

    private void drawTriangleByDisplay(Graphics2D g2d, int displayIndex, boolean highlighted) {
        // Renk şeridi display indeksine göre (görsel tutarlılık)
        Color color = (displayIndex % 2 == 0) ? TRIANGLE_LIGHT : TRIANGLE_BLUE;
        Polygon tri = getTrianglePolygon(displayIndex);

        g2d.setColor(color);
        g2d.fillPolygon(tri);
        g2d.setColor(new Color(120, 95, 70, 100));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawPolygon(tri);

        if (highlighted) {
            g2d.setColor(HIGHLIGHT_FILL);
            g2d.fillPolygon(tri);
            g2d.setColor(HIGHLIGHT_BORDER);
            g2d.setStroke(new BasicStroke(4f));
            g2d.drawPolygon(tri);
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawPolygon(tri);
        }
    }

    private Polygon getTrianglePolygon(int displayIndex) {
        int[] xp, yp;
        if (displayIndex < 12) {
            int xBase  = calculateXBase(displayIndex, false);
            int bottom = BOARD_Y + BOARD_HEIGHT;
            int apex   = BOARD_Y + BOARD_HEIGHT - POINT_HEIGHT;
            xp = new int[]{ xBase, xBase + POINT_WIDTH, xBase + POINT_WIDTH / 2 };
            yp = new int[]{ bottom, bottom, apex };
        } else {
            int xBase = calculateXBase(displayIndex - 12, true);
            int top   = BOARD_Y;
            int apex  = BOARD_Y + POINT_HEIGHT;
            xp = new int[]{ xBase, xBase + POINT_WIDTH, xBase + POINT_WIDTH / 2 };
            yp = new int[]{ top, top, apex };
        }
        return new Polygon(xp, yp, 3);
    }

    private void drawHighlights(Graphics2D g2d) {
        for (int boardIndex : highlightedPoints) {
            if (boardIndex < 0) continue; // -2 (tray) atla
            drawTriangleByDisplay(g2d, toDisplay(boardIndex), true);
        }
    }

   
    private void drawPointNumbers(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();

        for (int displayIndex = 0; displayIndex < 24; displayIndex++) {
            int boardIndex = toBoard(displayIndex);
            String label   = String.valueOf(boardIndex + 1); // hane numarası

            int x, y;
            if (displayIndex < 12) {
                int xBase = calculateXBase(displayIndex, false);
                x = xBase + POINT_WIDTH / 2 - fm.stringWidth(label) / 2;
                y = BOARD_Y + BOARD_HEIGHT - 8;
            } else {
                int xBase = calculateXBase(displayIndex - 12, true);
                x = xBase + POINT_WIDTH / 2 - fm.stringWidth(label) / 2;
                y = BOARD_Y + 15;
            }

            drawNumberBackground(g2d, x, y, label, fm);
            g2d.setColor(new Color(50, 35, 25));
            g2d.drawString(label, x, y);
        }
    }

    private void drawNumberBackground(Graphics2D g2d, int x, int y,
                                       String text, FontMetrics fm) {
        int px = 4, py = 2;
        g2d.setColor(new Color(255, 248, 235, 190));
        g2d.fillRoundRect(x - px, y - fm.getAscent() - py + 2,
                fm.stringWidth(text) + px * 2, fm.getAscent() + py * 2, 8, 8);
    }



    public void drawBarArea(Graphics2D g2d) {
        int barX = BOARD_X + 6 * POINT_WIDTH;
        GradientPaint gp = new GradientPaint(
                barX, BOARD_Y, new Color(165, 130, 90),
                barX + BAR_WIDTH, BOARD_Y, BAR_COLOR.darker());
        g2d.setPaint(gp);
        g2d.fillRect(barX, BOARD_Y, BAR_WIDTH, BOARD_HEIGHT);
        g2d.setColor(new Color(95, 65, 40));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRect(barX, BOARD_Y, BAR_WIDTH, BOARD_HEIGHT);
    }

  

    public void drawPieces(Graphics2D g2d, Board board) {
        if (board == null) return;
        Point[] points = board.getPoints();

        for (int boardIndex = 0; boardIndex < Board.POINT_COUNT; boardIndex++) {
            Point p = points[boardIndex];
            if (p == null || p.isEmpty()) continue;

            int count = p.getCount();
            int selectedDrawIndex = Math.min(count - 1, 4);

            for (int j = 0; j < count; j++) {
                if (j > 4) continue;

                Color pieceColor;
                if (boardIndex == selectedPoint && j == selectedDrawIndex) {
                    pieceColor = SELECTED_PIECE_RED;
                } else {
                    pieceColor = (p.getOwner() == Player.WHITE) ? PIECE_WHITE : PIECE_BLACK;
                }

                // Ekran pozisyonu display indeksinden hesaplanır
                int displayIndex = toDisplay(boardIndex);
                int[] pos = getPiecePositionByDisplay(displayIndex, j);

                String countText = (count > 5 && j == 4) ? String.valueOf(count) : null;
                drawPiece(g2d, pos[0], pos[1], pieceColor, countText);
            }
        }
    }

    public void drawBarPieces(Graphics2D g2d, Player player1, Player player2) {
        if (player1 == null || player2 == null) return;

        int barX      = BOARD_X + 6 * POINT_WIDTH;
        int pieceSize = 32;
        int centerX   = barX + (BAR_WIDTH - pieceSize) / 2;
        int midY      = BOARD_Y + BOARD_HEIGHT / 2;
        int spacing   = pieceSize + 4;

        for (Player p : new Player[]{ player1, player2 }) {
            if (p == null) continue;
            int count = p.getPiecesOnBar();
            if (count <= 0) continue;

            Color pieceColor = (p.getColor() == Player.WHITE) ? PIECE_WHITE : PIECE_BLACK;

            
            boolean goDown = (p.getColor() == localPlayerColor);

            int showCount = Math.min(count, 4);
            for (int i = 0; i < showCount; i++) {
                int py = goDown
                        ? midY + 4 + i * spacing
                        : midY - 4 - pieceSize - i * spacing;
                String label = (i == showCount - 1 && count > 1) ? String.valueOf(count) : null;
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
            g2d.drawString(label,
                    x + (size - fm.stringWidth(label)) / 2,
                    y + (size + fm.getAscent()) / 2 - 3);
        }
    }

 
    public void drawBorneOffTray(Graphics2D g2d, Player player1, Player player2) {
        if (player1 == null || player2 == null) return;
        for (Player p : new Player[]{ player1, player2 }) {
            if (p == null) continue;
            boolean isLocal  = (p.getColor() == localPlayerColor);
            Color pieceColor = (p.getColor() == Player.WHITE) ? PIECE_WHITE : PIECE_BLACK;
            int trayY = isLocal ? TRAY_BOT_Y : TRAY_TOP_Y;
            drawBorneOffBox(g2d, TRAY_X, trayY, TRAY_WIDTH, TRAY_HEIGHT,
                    pieceColor, p.getPiecesBorneOff());
        }
    }

    private void drawBorneOffBox(Graphics2D g2d, int x, int y, int width, int height,
                                  Color pieceColor, int count) {
        boolean isWhitePiece = pieceColor.equals(PIECE_WHITE);
        Color bgColor = isWhitePiece
                ? new Color(240, 225, 195, 220)
                : new Color(195, 175, 145, 220);
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, width, height, 14, 14);

        Color borderColor = count > 0 ? new Color(80, 180, 80) : new Color(160, 130, 90);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(count > 0 ? 2.5f : 1.5f));
        g2d.drawRoundRect(x, y, width, height, 14, 14);

        int smallSize = 16;
        int simX      = x + (width - smallSize) / 2;
        int padding   = 8;
        int maxShow   = Math.min(count, 8);
        int simStartY = y + padding;
        for (int i = 0; i < maxShow; i++) {
            int simY = simStartY + i * (smallSize + 2);
            if (simY + smallSize > y + height - 26) break;
            drawSmallPiece(g2d, simX, simY, smallSize, pieceColor);
        }

        String numStr = String.valueOf(count);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(count > 0 ? new Color(30, 120, 30) : new Color(120, 100, 70));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(numStr, x + (width - fm.stringWidth(numStr)) / 2, y + height - 8);
    }

    public void drawCapturedPieces(Graphics2D g2d, Player player1, Player player2) {
        drawBorneOffTray(g2d, player1, player2);
    }

    private void drawSmallPiece(Graphics2D g2d, int x, int y, int size, Color color) {
        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.fillOval(x + 3, y + 3, size, size);
        GradientPaint gp = new GradientPaint(x, y, color.brighter(),
                x, y + size, color.darker());
        g2d.setPaint(gp);
        g2d.fillOval(x, y, size, size);
        g2d.setColor(new Color(90, 65, 40));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, size, size);
        g2d.setColor(new Color(255, 255, 255, 70));
        g2d.drawOval(x + 3, y + 3, size - 6, size - 6);
    }



    public void drawDice(Graphics2D g2d, Dice dice) {
        int diceSize = 38, gap = 10;
        int barRightX        = BOARD_X + 6 * POINT_WIDTH + BAR_WIDTH;
        int rightAreaWidth   = BOARD_X + BOARD_WIDTH - barRightX;
        int rightAreaCenterX = barRightX + rightAreaWidth / 2;
        int boardCenterY     = BOARD_Y + BOARD_HEIGHT / 2;

        int remainingCount = dice.getRemainingMoves().size();
        int startX = rightAreaCenterX - (2 * diceSize + gap) / 2;
        int startY = boardCenterY - diceSize / 2;

        if (dice.isDoubles()) {
            drawSingleDie(g2d, startX, startY, diceSize, dice.getDie1(), remainingCount >= 1);
            drawSingleDie(g2d, startX + diceSize + gap, startY, diceSize, dice.getDie2(), remainingCount >= 2);
            if (remainingCount > 0) {
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.setColor(new Color(255, 230, 80));
                String rt = "x" + remainingCount;
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(rt, rightAreaCenterX - fm.stringWidth(rt) / 2,
                        startY + diceSize + 18);
            }
        } else {
            drawSingleDie(g2d, startX, startY, diceSize, dice.getDie1(), dice.canUse(dice.getDie1()));
            drawSingleDie(g2d, startX + diceSize + gap, startY, diceSize, dice.getDie2(), dice.canUse(dice.getDie2()));
        }
    }

    private void drawSingleDie(Graphics2D g2d, int x, int y, int size,
                                int value, boolean active) {
        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillRoundRect(x + 4, y + 5, size, size, 14, 14);
        GradientPaint gp = new GradientPaint(
                x, y, active ? new Color(20, 185, 255) : new Color(95, 145, 170),
                x + size, y + size, active ? DICE_BLUE_DARK : new Color(70, 90, 105));
        g2d.setPaint(gp);
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

    private void drawDiceDots(Graphics2D g2d, int x, int y, int size, int value) {
        int dotSize = Math.max(5, size / 5);
        int pad = size / 4;
        int midX = x + size / 2, midY = y + size / 2;
        int left = x + pad, right = x + size - pad;
        int top  = y + pad, bottom = y + size - pad;

        int[][] positions = {
                {left, top}, {right, top}, {left, midY},
                {midX, midY}, {right, midY}, {left, bottom}, {right, bottom}
        };
        int[][] dotLayout = {
                {}, {3}, {0, 6}, {0, 3, 6},
                {0, 1, 5, 6}, {0, 1, 3, 5, 6}, {0, 1, 2, 4, 5, 6}
        };
        if (value >= 1 && value <= 6) {
            for (int idx : dotLayout[value]) {
                g2d.fillOval(positions[idx][0] - dotSize / 2,
                             positions[idx][1] - dotSize / 2, dotSize, dotSize);
            }
        }
    }

    private void drawPiece(Graphics2D g2d, int x, int y, Color color, String countText) {
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(x + 4, y + 4, PIECE_DIAMETER, PIECE_DIAMETER);
        GradientPaint gp = new GradientPaint(x, y, color.brighter(),
                x, y + PIECE_DIAMETER, color.darker());
        g2d.setPaint(gp);
        g2d.fill(new Ellipse2D.Double(x, y, PIECE_DIAMETER, PIECE_DIAMETER));
        g2d.setColor(PIECE_BORDER);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(x, y, PIECE_DIAMETER, PIECE_DIAMETER);
        g2d.setColor(new Color(255, 255, 255, 75));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(x + 4, y + 4, PIECE_DIAMETER - 8, PIECE_DIAMETER - 8);
        if (countText != null) {
            g2d.setColor(color.equals(PIECE_WHITE) ? Color.BLACK : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(countText,
                    x + (PIECE_DIAMETER - fm.stringWidth(countText)) / 2,
                    y + (PIECE_DIAMETER + fm.getAscent()) / 2 - 3);
        }
    }

    private int calculateXBase(int displayCol, boolean isTop) {
        int barX = BOARD_X + 6 * POINT_WIDTH;
        if (isTop) {
            return (displayCol < 6)
                    ? BOARD_X + BOARD_WIDTH - (displayCol + 1) * POINT_WIDTH
                    : BOARD_X + (11 - displayCol) * POINT_WIDTH;
        } else {
            return (displayCol < 6)
                    ? BOARD_X + displayCol * POINT_WIDTH
                    : barX + BAR_WIDTH + (displayCol - 6) * POINT_WIDTH;
        }
    }

  
    private int[] getPiecePositionByDisplay(int displayIndex, int stackIndex) {
        int[] center = getPointCenterByDisplay(displayIndex);
        int offset   = stackIndex * (PIECE_DIAMETER + 3);
        int x = center[0] - PIECE_DIAMETER / 2;
        int y = (displayIndex < 12)
                ? BOARD_Y + BOARD_HEIGHT - PIECE_DIAMETER - 25 - offset
                : BOARD_Y + 25 + offset;
        return new int[]{ x, y };
    }


    private int[] getPointCenterByDisplay(int displayIndex) {
        int xBase, y;
        if (displayIndex < 12) {
            xBase = calculateXBase(displayIndex, false);
            y     = BOARD_Y + BOARD_HEIGHT - POINT_HEIGHT / 2;
        } else {
            xBase = calculateXBase(displayIndex - 12, true);
            y     = BOARD_Y + POINT_HEIGHT / 2;
        }
        return new int[]{ xBase + POINT_WIDTH / 2, y };
    }

   
    public int[] getPointCenter(int boardIndex) {
        return getPointCenterByDisplay(toDisplay(boardIndex));
    }

   
    public int getPointIndexAt(int mouseX, int mouseY) {
        for (int displayIndex = 0; displayIndex < Board.POINT_COUNT; displayIndex++) {
            int[] center = getPointCenterByDisplay(displayIndex);
            int halfW    = POINT_WIDTH / 2;

            if (mouseX >= center[0] - halfW && mouseX <= center[0] + halfW) {
                if (displayIndex < 12) {
                    if (mouseY >= BOARD_Y + BOARD_HEIGHT - POINT_HEIGHT
                            && mouseY <= BOARD_Y + BOARD_HEIGHT) {
                        return toBoard(displayIndex);
                    }
                } else {
                    if (mouseY >= BOARD_Y && mouseY <= BOARD_Y + POINT_HEIGHT) {
                        return toBoard(displayIndex);
                    }
                }
            }
        }
        return -1;
    }

    public boolean isBarClicked(int mouseX, int mouseY) {
        int barX = BOARD_X + 6 * POINT_WIDTH;
        return mouseX >= barX && mouseX <= barX + BAR_WIDTH
                && mouseY >= BOARD_Y && mouseY <= BOARD_Y + BOARD_HEIGHT;
    }

   
    public boolean isTrayClicked(int mouseX, int mouseY, int playerColor) {
        int trayY = (playerColor == localPlayerColor) ? TRAY_BOT_Y : TRAY_TOP_Y;
        return mouseX >= TRAY_X && mouseX <= TRAY_X + TRAY_WIDTH
                && mouseY >= trayY && mouseY <= trayY + TRAY_HEIGHT;
    }



    public void setHighlightedPoints(List<Integer> points) {
        this.highlightedPoints = points;
    }

    /** boardIndex alır — drawPieces ile tutarlı. */
    public void setSelectedPoint(int boardIndex) {
        this.selectedPoint = boardIndex;
    }

    public void clearSelection() {
        this.selectedPoint    = -1;
        this.highlightedPoints = null;
    }
}