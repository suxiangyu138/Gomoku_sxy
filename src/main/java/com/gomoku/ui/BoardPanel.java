package com.gomoku.ui;

import com.gomoku.ai.AIPlayer;
import com.gomoku.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 * 棋盘面板：现代极简风格，大尺寸棋盘，写实棋子质感。
 */
public class BoardPanel extends JPanel {

    // ---- 布局常量（加大棋盘） ----
    public static final int CELL = 48;
    public static final int MARGIN = 50;
    public static final int PIECE_R = 20;
    private static final int SIZE = GameState.SIZE;
    public static final int BOARD_PX = MARGIN * 2 + CELL * (SIZE - 1);

    private static final int PAD = 24;

    // ---- 颜色系统 ----
    private static final Color BG_COLOR      = new Color(26, 26, 46);
    private static final Color BOARD_COLOR   = new Color(235, 226, 212);
    private static final Color BOARD_SHADOW  = new Color(10, 10, 25, 70);
    private static final Color GRID_COLOR    = new Color(175, 160, 140);
    private static final Color STAR_COLOR    = new Color(145, 130, 110);
    private static final Color LABEL_COLOR   = new Color(145, 130, 110);
    private static final Color ACCENT_COLOR  = new Color(212, 168, 83);
    private static final Color LAST_DOT      = new Color(220, 80, 80);
    private static final Color BOARD_BORDER  = new Color(190, 178, 162);

    // 星位坐标
    private static final int[][] STARS = {{3, 3}, {3, 11}, {7, 7}, {11, 3}, {11, 11}};

    // ---- 状态 ----
    private final GameState state;
    private ControlPanel controlPanel;
    private int hoverR = -1;
    private int hoverC = -1;
    private boolean vsAI = true;
    private final int aiPlayer = GameState.WHITE;
    private boolean aiThinking;

    // 动画
    private float winGlowPhase;
    private final Timer animTimer;

    public BoardPanel(GameState state) {
        this.state = state;
        int panelSize = BOARD_PX + PAD * 2;
        setPreferredSize(new Dimension(panelSize, panelSize));
        setOpaque(true);
        setBackground(BG_COLOR);

        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);

        animTimer = new Timer(30, e -> {
            if (state.winCells != null) {
                winGlowPhase += 0.06f;
                repaint();
            }
        });
        animTimer.start();
    }

    // ---- getters / setters ----

    public void setControlPanel(ControlPanel cp) { this.controlPanel = cp; }
    public GameState getState() { return state; }
    public boolean isVsAI() { return vsAI; }
    public int getAiPlayer() { return aiPlayer; }
    public boolean isAiThinking() { return aiThinking; }

    // ==================== 主绘制 ====================

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int ox = (getWidth() - BOARD_PX) / 2;
        int oy = (getHeight() - BOARD_PX) / 2;
        g.translate(ox, oy);

        drawBoardShadow(g);
        drawBoardSurface(g);
        drawGrid(g);
        drawStars(g);
        drawCoordinates(g);
        drawPieces(g);
        drawLastMoveMark(g);
        drawWinHighlight(g);
        drawHoverPreview(g);

        g.translate(-ox, -oy);
    }

    // ==================== 棋盘 ====================

    private void drawBoardShadow(Graphics2D g) {
        g.setColor(BOARD_SHADOW);
        g.fill(new RoundRectangle2D.Float(3, 4, BOARD_PX, BOARD_PX, 14, 14));
    }

    private void drawBoardSurface(Graphics2D g) {
        g.setColor(BOARD_COLOR);
        g.fill(new RoundRectangle2D.Float(0, 0, BOARD_PX, BOARD_PX, 12, 12));

        // 内边框
        g.setStroke(new BasicStroke(1.6f));
        g.setColor(BOARD_BORDER);
        g.draw(new RoundRectangle2D.Float(1, 1, BOARD_PX - 2, BOARD_PX - 2, 10, 10));
    }

    // ==================== 网格线 ====================

    private void drawGrid(Graphics2D g) {
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(GRID_COLOR);
        int end = MARGIN + CELL * (SIZE - 1);
        for (int i = 0; i < SIZE; i++) {
            int p = MARGIN + i * CELL;
            g.drawLine(MARGIN, p, end, p);
            g.drawLine(p, MARGIN, p, end);
        }
    }

    private void drawStars(Graphics2D g) {
        g.setColor(STAR_COLOR);
        for (int[] s : STARS) {
            int sx = MARGIN + s[1] * CELL;
            int sy = MARGIN + s[0] * CELL;
            g.fillOval(sx - 4, sy - 4, 8, 8);
        }
    }

    private void drawCoordinates(Graphics2D g) {
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 11);
        g.setFont(font);
        g.setColor(LABEL_COLOR);
        FontMetrics fm = g.getFontMetrics();

        for (int i = 0; i < SIZE; i++) {
            int pos = MARGIN + i * CELL;

            // 列标 A-O（顶部 & 底部）
            String col = String.valueOf((char) ('A' + i));
            int cw = fm.stringWidth(col);
            int ch = fm.getAscent();
            // 顶部：baseline 在 MARGIN 上方留出 14px 间距
            g.drawString(col, pos - cw / 2, MARGIN - 14 + ch / 3);
            // 底部
            g.drawString(col, pos - cw / 2, BOARD_PX - MARGIN + 22 + ch / 3);

            // 行标 1-15（左侧 & 右侧）
            String row = String.valueOf(SIZE - i);
            int rw = fm.stringWidth(row);
            // 左侧
            g.drawString(row, MARGIN - rw - 16, pos + ch / 3);
            // 右侧
            g.drawString(row, BOARD_PX - MARGIN + 20, pos + ch / 3);
        }
    }

    // ==================== 棋子 ====================

    private void drawPieces(Graphics2D g) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (state.board[r][c] != GameState.EMPTY) {
                    drawPiece(g, r, c, state.board[r][c], 1.0f);
                }
            }
        }
    }

    private void drawPiece(Graphics2D g, int r, int c, int player, float alpha) {
        int cx = MARGIN + c * CELL;
        int cy = MARGIN + r * CELL;
        int d = PIECE_R * 2;

        Composite oldComposite = g.getComposite();
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        // 阴影（offset 右下）
        if (alpha > 0.5f) {
            g.setColor(new Color(0, 0, 0, (int) (alpha * 45)));
            g.fill(new Ellipse2D.Float(cx - PIECE_R + 2, cy - PIECE_R + 3, d, d));
        }

        Ellipse2D.Float ellipse = new Ellipse2D.Float(cx - PIECE_R, cy - PIECE_R, d, d);

        // 径向渐变（光照在左上）
        if (player == GameState.BLACK) {
            float[] dist = {0.0f, 0.35f, 0.7f, 1.0f};
            Color[] colors = {
                new Color(105, 105, 110),
                new Color(50, 50, 55),
                new Color(18, 18, 22),
                new Color(35, 35, 40)
            };
            g.setPaint(new RadialGradientPaint(
                    cx - PIECE_R * 0.3f, cy - PIECE_R * 0.35f, PIECE_R * 1.15f, dist, colors));
        } else {
            float[] dist = {0.0f, 0.3f, 0.65f, 1.0f};
            Color[] colors = {
                new Color(255, 255, 255),
                new Color(245, 243, 238),
                new Color(210, 205, 195),
                new Color(225, 220, 212)
            };
            g.setPaint(new RadialGradientPaint(
                    cx - PIECE_R * 0.3f, cy - PIECE_R * 0.35f, PIECE_R * 1.2f, dist, colors));
        }
        g.fill(ellipse);

        // 白棋边框
        if (player == GameState.WHITE) {
            g.setStroke(new BasicStroke(0.8f));
            g.setColor(new Color(160, 150, 140, (int) (alpha * 180)));
            g.draw(ellipse);
        }

        // 高光点
        int hlSize = PIECE_R * 2 / 5;
        g.setColor(new Color(255, 255, 255, player == GameState.BLACK
                ? (int) (alpha * 50) : (int) (alpha * 140)));
        g.fill(new Ellipse2D.Float(cx - hlSize * 0.6f, cy - hlSize * 0.8f, hlSize, hlSize));

        g.setComposite(oldComposite);
    }

    // ==================== 标记与高亮 ====================

    private void drawLastMoveMark(Graphics2D g) {
        if (state.history.isEmpty() || state.gameOver) return;
        int[] last = state.history.get(state.history.size() - 1);
        int cx = MARGIN + last[1] * CELL;
        int cy = MARGIN + last[0] * CELL;
        g.setColor(LAST_DOT);
        g.fillOval(cx - 4, cy - 4, 8, 8);
    }

    private void drawWinHighlight(Graphics2D g) {
        int[] wc = state.winCells;
        if (wc == null) return;
        int n = wc.length / 2;
        if (n < 5) return;

        int minR = wc[0], minC = wc[1], maxR = wc[0], maxC = wc[1];
        for (int i = 1; i < n; i++) {
            if (wc[i * 2] < minR || (wc[i * 2] == minR && wc[i * 2 + 1] < minC)) {
                minR = wc[i * 2]; minC = wc[i * 2 + 1];
            }
            if (wc[i * 2] > maxR || (wc[i * 2] == maxR && wc[i * 2 + 1] > maxC)) {
                maxR = wc[i * 2]; maxC = wc[i * 2 + 1];
            }
        }
        int x1 = MARGIN + minC * CELL, y1 = MARGIN + minR * CELL;
        int x2 = MARGIN + maxC * CELL, y2 = MARGIN + maxR * CELL;

        float pulse = (float) (Math.sin(winGlowPhase) * 0.3f + 0.7f);

        // 发光连线
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 210, 80, (int) (50 + pulse * 30)));
        g.draw(new Line2D.Float(x1, y1, x2, y2));

        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(245, 197, 24, (int) (170 + pulse * 60)));
        g.draw(new Line2D.Float(x1, y1, x2, y2));

        // 光晕环
        g.setStroke(new BasicStroke(3f));
        for (int i = 0; i < n; i++) {
            int cx = MARGIN + wc[i * 2 + 1] * CELL;
            int cy = MARGIN + wc[i * 2] * CELL;
            g.setColor(new Color(245, 197, 24, (int) (120 + pulse * 80)));
            g.drawOval(cx - PIECE_R - 4, cy - PIECE_R - 4, (PIECE_R + 4) * 2, (PIECE_R + 4) * 2);
        }
    }

    private void drawHoverPreview(Graphics2D g) {
        if (hoverR < 0 || hoverC < 0 || state.gameOver || aiThinking) return;
        if (state.board[hoverR][hoverC] != GameState.EMPTY) return;

        drawPiece(g, hoverR, hoverC, state.currentPlayer, 0.38f);

        // 十字准心
        int cx = MARGIN + hoverC * CELL;
        int cy = MARGIN + hoverR * CELL;
        int gap = PIECE_R + 5;
        int len = 8;
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 100));
        g.drawLine(cx, cy - gap, cx, cy - gap - len);
        g.drawLine(cx, cy + gap, cx, cy + gap + len);
        g.drawLine(cx - gap, cy, cx - gap - len, cy);
        g.drawLine(cx + gap, cy, cx + gap + len, cy);
    }

    // ==================== 鼠标处理 ====================

    private static int[] toGrid(int mouseX, int mouseY) {
        int c = Math.round((float) (mouseX - MARGIN) / CELL);
        int r = Math.round((float) (mouseY - MARGIN) / CELL);
        if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return null;
        return new int[]{r, c};
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (state.gameOver || aiThinking) return;
            if (vsAI && state.currentPlayer == aiPlayer) return;

            int[] pos = toGrid(e.getX(), e.getY());
            if (pos == null) return;

            if (state.place(pos[0], pos[1])) {
                repaint();
                if (controlPanel != null) {
                    controlPanel.refreshUI();
                    if (state.gameOver) { controlPanel.showResultDialog(); return; }
                }
                if (!state.gameOver && vsAI) {
                    aiThinking = true;
                    repaint();
                    if (controlPanel != null) controlPanel.refreshUI();
                    new Thread(() -> {
                        try { Thread.sleep(180); }
                        catch (InterruptedException ex) { Thread.currentThread().interrupt(); return; }
                        int[] move = AIPlayer.findBestMove(state.board, aiPlayer);
                        SwingUtilities.invokeLater(() -> {
                            state.place(move[0], move[1]);
                            aiThinking = false;
                            repaint();
                            if (controlPanel != null) {
                                controlPanel.refreshUI();
                                if (state.gameOver) controlPanel.showResultDialog();
                            }
                        });
                    }).start();
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int[] pos = toGrid(e.getX(), e.getY());
            int nr = (pos != null) ? pos[0] : -1;
            int nc = (pos != null) ? pos[1] : -1;
            if (nr != hoverR || nc != hoverC) { hoverR = nr; hoverC = nc; repaint(); }
        }

        @Override
        public void mouseExited(MouseEvent e) { hoverR = hoverC = -1; repaint(); }
    }

    // ==================== 公共操作 ====================

    public void restart() {
        state.reset();
        hoverR = hoverC = -1;
        aiThinking = false;
        repaint();
    }

    public void undo() {
        if (state.gameOver) { state.gameOver = false; state.winCells = null; state.draw = false; }
        if (vsAI && state.history.size() >= 2) { state.undo(); state.undo(); }
        else if (!state.history.isEmpty()) { state.undo(); }
        repaint();
    }

    public void setVsAI(boolean ai) { this.vsAI = ai; restart(); }
}
