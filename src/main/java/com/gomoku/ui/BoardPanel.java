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
 * 棋盘面板：适中尺寸、精准点击映射、舒适配色。
 */
public class BoardPanel extends JPanel {

    public static final int CELL = 35;
    public static final int MARGIN = 38;
    public static final int PIECE_R = 15;
    private static final int SIZE = GameState.SIZE;
    public static final int BOARD_PX = MARGIN * 2 + CELL * (SIZE - 1); // 566

    private static final int PAD = 30; // 面板四周留白
    private static final int PANEL_SIZE = BOARD_PX + PAD * 2; // 626

    // 棋盘绘制原点（在面板坐标系中的偏移）
    private int boardOx;
    private int boardOy;

    private static final Color BG_COLOR      = new Color(30, 30, 50);
    private static final Color BOARD_COLOR   = new Color(238, 230, 218);
    private static final Color BOARD_SHADOW  = new Color(10, 10, 28, 80);
    private static final Color GRID_COLOR    = new Color(170, 158, 138);
    private static final Color STAR_COLOR    = new Color(148, 134, 114);
    private static final Color LABEL_COLOR   = new Color(152, 138, 118);
    private static final Color ACCENT_COLOR  = new Color(212, 168, 83);
    private static final Color LAST_DOT      = new Color(225, 75, 75);
    private static final Color BOARD_BORDER  = new Color(185, 172, 155);
    private static final Color HOVER_CROSS   = new Color(212, 168, 83, 110);

    private static final int[][] STARS = {{3, 3}, {3, 11}, {7, 7}, {11, 3}, {11, 11}};

    private final GameState state;
    private ControlPanel controlPanel;
    private int hoverR = -1, hoverC = -1;
    private boolean vsAI = true;
    private final int aiPlayer = GameState.WHITE;
    private boolean aiThinking;
    private float winGlowPhase;
    private final Timer animTimer;

    public BoardPanel(GameState state) {
        this.state = state;
        setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
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

        // 每次重绘重新计算居中偏移
        boardOx = (getWidth() - BOARD_PX) / 2;
        boardOy = (getHeight() - BOARD_PX) / 2;
        g.translate(boardOx, boardOy);

        drawBoardShadow(g);
        drawBoardSurface(g);
        drawGrid(g);
        drawStars(g);
        drawCoordinates(g);
        drawPieces(g);
        drawLastMoveMark(g);
        drawWinHighlight(g);
        drawHoverPreview(g);

        g.translate(-boardOx, -boardOy);
    }

    // ==================== 棋盘 ====================

    private void drawBoardShadow(Graphics2D g) {
        g.setColor(BOARD_SHADOW);
        g.fill(new RoundRectangle2D.Float(3, 4, BOARD_PX, BOARD_PX, 16, 16));
    }

    private void drawBoardSurface(Graphics2D g) {
        g.setColor(BOARD_COLOR);
        g.fill(new RoundRectangle2D.Float(0, 0, BOARD_PX, BOARD_PX, 14, 14));

        g.setStroke(new BasicStroke(1.6f));
        g.setColor(BOARD_BORDER);
        g.draw(new RoundRectangle2D.Float(1, 1, BOARD_PX - 2, BOARD_PX - 2, 12, 12));
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
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        g.setColor(LABEL_COLOR);
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < SIZE; i++) {
            int pos = MARGIN + i * CELL;
            String col = String.valueOf((char) ('A' + i));
            int cw = fm.stringWidth(col);
            int cy = fm.getAscent() / 2;
            g.drawString(col, pos - cw / 2, MARGIN - 18 + cy);
            g.drawString(col, pos - cw / 2, BOARD_PX - MARGIN + 24 + cy);

            String row = String.valueOf(SIZE - i);
            int rw = fm.stringWidth(row);
            g.drawString(row, MARGIN - rw - 18, pos + cy);
            g.drawString(row, BOARD_PX - MARGIN + 22, pos + cy);
        }
    }

    // ==================== 棋子 ====================

    private void drawPieces(Graphics2D g) {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (state.board[r][c] != GameState.EMPTY)
                    drawPiece(g, r, c, state.board[r][c], 1.0f);
    }

    private void drawPiece(Graphics2D g, int r, int c, int player, float alpha) {
        int cx = MARGIN + c * CELL;
        int cy = MARGIN + r * CELL;
        int d = PIECE_R * 2;

        Composite old = g.getComposite();
        if (alpha < 1f)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        Ellipse2D.Float ellipse = new Ellipse2D.Float(cx - PIECE_R, cy - PIECE_R, d, d);

        if (alpha > 0.5f) {
            g.setColor(new Color(0, 0, 0, (int) (alpha * 48)));
            g.fill(new Ellipse2D.Float(cx - PIECE_R + 2, cy - PIECE_R + 3, d, d));
        }

        float fx = cx - PIECE_R * 0.3f;
        float fy = cy - PIECE_R * 0.35f;
        if (player == GameState.BLACK) {
            g.setPaint(new RadialGradientPaint(fx, fy, PIECE_R * 1.15f,
                    new float[]{0f, 0.35f, 0.7f, 1f},
                    new Color[]{new Color(108, 108, 113), new Color(48, 48, 53),
                                new Color(16, 16, 20), new Color(38, 38, 43)}));
        } else {
            g.setPaint(new RadialGradientPaint(fx, fy, PIECE_R * 1.2f,
                    new float[]{0f, 0.3f, 0.65f, 1f},
                    new Color[]{new Color(255, 255, 255), new Color(245, 243, 238),
                                new Color(208, 203, 193), new Color(228, 223, 215)}));
        }
        g.fill(ellipse);

        if (player == GameState.WHITE) {
            g.setStroke(new BasicStroke(0.9f));
            g.setColor(new Color(158, 148, 138, (int) (alpha * 180)));
            g.draw(ellipse);
        }

        int hl = PIECE_R * 2 / 5;
        g.setColor(new Color(255, 255, 255,
                player == GameState.BLACK ? (int) (alpha * 50) : (int) (alpha * 140)));
        g.fill(new Ellipse2D.Float(cx - hl * 0.55f, cy - hl * 0.75f, hl, hl));

        g.setComposite(old);
    }

    // ==================== 标记 ====================

    private void drawLastMoveMark(Graphics2D g) {
        if (state.history.isEmpty() || state.gameOver) return;
        int[] last = state.history.get(state.history.size() - 1);
        int cx = MARGIN + last[1] * CELL, cy = MARGIN + last[0] * CELL;
        g.setColor(LAST_DOT);
        g.fillOval(cx - 4, cy - 4, 9, 9);
    }

    private void drawWinHighlight(Graphics2D g) {
        int[] wc = state.winCells;
        if (wc == null) return;
        int n = wc.length / 2;
        if (n < 5) return;

        int minR = wc[0], minC = wc[1], maxR = wc[0], maxC = wc[1];
        for (int i = 1; i < n; i++) {
            if (wc[i * 2] < minR || (wc[i * 2] == minR && wc[i * 2 + 1] < minC)) {
                minR = wc[i * 2]; minC = wc[i * 2 + 1]; }
            if (wc[i * 2] > maxR || (wc[i * 2] == maxR && wc[i * 2 + 1] > maxC)) {
                maxR = wc[i * 2]; maxC = wc[i * 2 + 1]; }
        }
        int x1 = MARGIN + minC * CELL, y1 = MARGIN + minR * CELL;
        int x2 = MARGIN + maxC * CELL, y2 = MARGIN + maxR * CELL;

        float pulse = (float) (Math.sin(winGlowPhase) * 0.3f + 0.7f);

        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 210, 80, (int) (45 + pulse * 30)));
        g.draw(new Line2D.Float(x1, y1, x2, y2));

        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(245, 197, 24, (int) (160 + pulse * 60)));
        g.draw(new Line2D.Float(x1, y1, x2, y2));

        g.setStroke(new BasicStroke(3f));
        for (int i = 0; i < n; i++) {
            int cx = MARGIN + wc[i * 2 + 1] * CELL, cy = MARGIN + wc[i * 2] * CELL;
            g.setColor(new Color(245, 197, 24, (int) (120 + pulse * 80)));
            g.drawOval(cx - PIECE_R - 5, cy - PIECE_R - 5, (PIECE_R + 5) * 2, (PIECE_R + 5) * 2);
        }
    }

    private void drawHoverPreview(Graphics2D g) {
        if (hoverR < 0 || hoverC < 0 || state.gameOver || aiThinking) return;
        if (state.board[hoverR][hoverC] != GameState.EMPTY) return;

        drawPiece(g, hoverR, hoverC, state.currentPlayer, 0.38f);

        int cx = MARGIN + hoverC * CELL, cy = MARGIN + hoverR * CELL;
        int gap = PIECE_R + 6, len = 9;
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(HOVER_CROSS);
        g.drawLine(cx, cy - gap, cx, cy - gap - len);
        g.drawLine(cx, cy + gap, cx, cy + gap + len);
        g.drawLine(cx - gap, cy, cx - gap - len, cy);
        g.drawLine(cx + gap, cy, cx + gap + len, cy);
    }

    // ==================== 鼠标 — 修正偏移 ====================

    /**
     * 将面板坐标映射到棋盘网格坐标。
     * 减去 (boardOx, boardOy) 补偿画布居中偏移。
     */
    private int[] toGrid(int mouseX, int mouseY) {
        int x = mouseX - boardOx;
        int y = mouseY - boardOy;
        // 距离最近交叉点的容差（CELL / 2.5 ≈ 20px）
        int c = Math.round((float) (x - MARGIN) / CELL);
        int r = Math.round((float) (y - MARGIN) / CELL);
        if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return null;

        // 检查是否在交叉点合理范围内
        int cx = MARGIN + c * CELL;
        int cy = MARGIN + r * CELL;
        int dx = x - cx, dy = y - cy;
        int snapRange = CELL / 2;
        if (dx * dx + dy * dy > snapRange * snapRange) return null;

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
            int nr = (pos != null) ? pos[0] : -1, nc = (pos != null) ? pos[1] : -1;
            if (nr != hoverR || nc != hoverC) { hoverR = nr; hoverC = nc; repaint(); }
        }

        @Override
        public void mouseExited(MouseEvent e) { hoverR = hoverC = -1; repaint(); }
    }

    // ==================== 公共操作 ====================

    public void restart() { state.reset(); hoverR = hoverC = -1; aiThinking = false; repaint(); }

    public void undo() {
        if (state.gameOver) { state.gameOver = false; state.winCells = null; state.draw = false; }
        if (vsAI && state.history.size() >= 2) { state.undo(); state.undo(); }
        else if (!state.history.isEmpty()) { state.undo(); }
        repaint();
    }

    public void setVsAI(boolean ai) { this.vsAI = ai; restart(); }
}
