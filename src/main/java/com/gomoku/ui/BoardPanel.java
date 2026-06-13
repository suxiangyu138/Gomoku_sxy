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
 * 棋盘面板：现代极简风格渲染，真实棋子质感。
 */
public class BoardPanel extends JPanel {

    // ---- 布局常量 ----
    public static final int CELL = 42;
    public static final int MARGIN = 44;
    public static final int PIECE_R = 18;
    private static final int SIZE = GameState.SIZE;
    public static final int BOARD_PX = MARGIN * 2 + CELL * (SIZE - 1);

    // ---- 颜色系统 ----
    private static final Color BG_COLOR       = new Color(26, 26, 46);       // 深色背景
    private static final Color BOARD_COLOR    = new Color(235, 226, 212);   // 暖白棋盘
    private static final Color BOARD_SHADOW   = new Color(10, 10, 25, 80); // 棋盘阴影
    private static final Color GRID_COLOR     = new Color(175, 160, 140);  // 网格线
    private static final Color STAR_COLOR     = new Color(140, 125, 105);  // 星位
    private static final Color LABEL_COLOR    = new Color(145, 130, 110);  // 坐标
    private static final Color ACCENT_COLOR   = new Color(212, 168, 83);   // 金色强调
    private static final Color LAST_DOT       = new Color(220, 80, 80);    // 最后落子标记
    private static final Color WIN_GLOW       = new Color(245, 197, 24);   // 获胜高亮

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
    private float winGlowPhase = 0f;
    private Timer animTimer;

    public BoardPanel(GameState state) {
        this.state = state;
        setPreferredSize(new Dimension(BOARD_PX + 20, BOARD_PX + 20));
        setOpaque(true);
        setBackground(BG_COLOR);

        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);

        // 获胜动画
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
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 居中对齐棋盘
        int ox = (getWidth() - BOARD_PX) / 2;
        int oy = (getHeight() - BOARD_PX) / 2;
        g.translate(ox, oy);

        drawBackground(g);
        drawBoardShadow(g);
        drawBoardSurface(g);
        drawGrid(g);
        drawStars(g);
        drawLabels(g);
        drawPieces(g);
        drawLastMoveMark(g);
        drawWinHighlight(g);
        drawHoverPreview(g);

        g.translate(-ox, -oy);
    }

    // ==================== 背景 ====================

    private void drawBackground(Graphics2D g) {
        // 细微渐变
        GradientPaint bg = new GradientPaint(0, 0, new Color(26, 26, 46),
                BOARD_PX, BOARD_PX, new Color(22, 22, 40));
        g.setPaint(bg);
        g.fillRect(-20, -20, BOARD_PX + 40, BOARD_PX + 40);
    }

    private void drawBoardShadow(Graphics2D g) {
        g.setColor(BOARD_SHADOW);
        g.fill(new RoundRectangle2D.Float(2, 2, BOARD_PX, BOARD_PX, 12, 12));
    }

    // ==================== 棋盘 ====================

    private void drawBoardSurface(Graphics2D g) {
        // 主色
        g.setColor(BOARD_COLOR);
        g.fill(new RoundRectangle2D.Float(0, 0, BOARD_PX, BOARD_PX, 10, 10));

        // 微妙纹理（模拟纸纹）
        g.setStroke(new BasicStroke(0.3f));
        for (int i = 0; i < 120; i++) {
            int alpha = 6 + (i * 7) % 9;
            g.setColor(new Color(200, 188, 170, alpha));
            int y = i * 5 + 2;
            int offset = (i * 3) % 7 - 3;
            g.drawLine(8, y, BOARD_PX - 8, y + offset);
        }

        // 内边框
        g.setStroke(new BasicStroke(1.8f));
        g.setColor(new Color(190, 178, 162));
        g.draw(new RoundRectangle2D.Float(1, 1, BOARD_PX - 2, BOARD_PX - 2, 8, 8));
    }

    // ==================== 网格线 ====================

    private void drawGrid(Graphics2D g) {
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(GRID_COLOR);
        for (int i = 0; i < SIZE; i++) {
            int p = MARGIN + i * CELL;
            g.drawLine(MARGIN, p, MARGIN + CELL * (SIZE - 1), p);
            g.drawLine(p, MARGIN, p, MARGIN + CELL * (SIZE - 1));
        }
    }

    private void drawStars(Graphics2D g) {
        g.setColor(STAR_COLOR);
        for (int[] s : STARS) {
            int sx = MARGIN + s[1] * CELL;
            int sy = MARGIN + s[0] * CELL;
            g.fillOval(sx - 3, sy - 3, 7, 7);
        }
    }

    private void drawLabels(Graphics2D g) {
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g.setColor(LABEL_COLOR);
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < SIZE; i++) {
            String col = String.valueOf((char) ('A' + i));
            int tx = MARGIN + i * CELL - fm.stringWidth(col) / 2;
            g.drawString(col, tx, MARGIN - 10);
            g.drawString(col, tx, BOARD_PX - MARGIN + 18);

            String row = String.valueOf(SIZE - i);
            int ty = MARGIN + i * CELL + fm.getAscent() / 2 - 1;
            g.drawString(row, MARGIN - fm.stringWidth(row) - 10, ty);
            g.drawString(row, BOARD_PX - MARGIN + 14, ty);
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

    /**
     * 使用 RadialGradientPaint 渲染写实棋子。
     */
    private void drawPiece(Graphics2D g, int r, int c, int player, float alpha) {
        int cx = MARGIN + c * CELL;
        int cy = MARGIN + r * CELL;
        int d = PIECE_R * 2;

        Composite oldComposite = g.getComposite();
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        Ellipse2D.Float ellipse = new Ellipse2D.Float(cx - PIECE_R, cy - PIECE_R, d, d);

        // 阴影
        if (alpha > 0.5f) {
            g.setColor(new Color(0, 0, 0, (int) (alpha * 50)));
            g.fill(new Ellipse2D.Float(cx - PIECE_R + 1, cy - PIECE_R + 2, d, d));
        }

        // 棋子主体（径向渐变模拟光照）
        if (player == GameState.BLACK) {
            float[] dist = {0.0f, 0.35f, 0.7f, 1.0f};
            Color[] colors = {
                new Color(105, 105, 110),
                new Color(50, 50, 55),
                new Color(18, 18, 22),
                new Color(35, 35, 40)
            };
            RadialGradientPaint p = new RadialGradientPaint(
                cx - PIECE_R * 0.3f, cy - PIECE_R * 0.35f, PIECE_R * 1.15f, dist, colors);
            g.setPaint(p);
        } else {
            float[] dist = {0.0f, 0.3f, 0.65f, 1.0f};
            Color[] colors = {
                new Color(255, 255, 255),
                new Color(245, 243, 238),
                new Color(210, 205, 195),
                new Color(225, 220, 212)
            };
            RadialGradientPaint p = new RadialGradientPaint(
                cx - PIECE_R * 0.3f, cy - PIECE_R * 0.35f, PIECE_R * 1.2f, dist, colors);
            g.setPaint(p);
        }
        g.fill(ellipse);

        // 边框
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

        // 找出两端
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

        // 发光连线
        float pulse = (float) (Math.sin(winGlowPhase) * 0.3 + 0.7);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 210, 80, (int) (40 + pulse * 30)));
        g.draw(new Line2D.Float(x1, y1, x2, y2));

        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(245, 197, 24, (int) (160 + pulse * 60)));
        g.draw(new Line2D.Float(x1, y1, x2, y2));

        // 获胜棋子光晕
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

        // 半透明预览
        drawPiece(g, hoverR, hoverC, state.currentPlayer, 0.38f);

        // 十字准心
        int cx = MARGIN + hoverC * CELL;
        int cy = MARGIN + hoverR * CELL;
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(state.currentPlayer == GameState.BLACK ? 80 : 200,
                state.currentPlayer == GameState.BLACK ? 80 : 200,
                state.currentPlayer == GameState.BLACK ? 80 : 200, 90));
        int gap = PIECE_R + 5;
        int len = 8;
        // 上
        g.drawLine(cx, cy - gap, cx, cy - gap - len);
        // 下
        g.drawLine(cx, cy + gap, cx, cy + gap + len);
        // 左
        g.drawLine(cx - gap, cy, cx - gap - len, cy);
        // 右
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
                // AI
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
