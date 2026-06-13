package com.gomoku.ui;

import com.gomoku.ai.AIPlayer;
import com.gomoku.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

/**
 * 棋盘面板：渲染棋盘并处理鼠标落子输入。
 */
public class BoardPanel extends JPanel {

    public static final int CELL = 40;
    public static final int MARGIN = 32;
    public static final int PIECE_R = 17;
    private static final int SIZE = GameState.SIZE;
    public static final int BOARD_PX = MARGIN * 2 + CELL * (SIZE - 1);

    private static final int[][] STARS = {{3, 3}, {3, 11}, {7, 7}, {11, 3}, {11, 11}};

    private final GameState state;
    private ControlPanel controlPanel;
    private int hoverR = -1;
    private int hoverC = -1;
    private boolean vsAI = true;
    private int aiPlayer = GameState.WHITE;
    private boolean aiThinking;

    public BoardPanel(GameState state) {
        this.state = state;
        setPreferredSize(new Dimension(BOARD_PX, BOARD_PX));
        setOpaque(true);
        setBackground(new Color(200, 155, 60));

        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    // ---- getters / setters ----

    public void setControlPanel(ControlPanel cp) { this.controlPanel = cp; }

    public GameState getState() { return state; }

    public boolean isVsAI() { return vsAI; }

    public int getAiPlayer() { return aiPlayer; }

    public boolean isAiThinking() { return aiThinking; }

    // ==================== 绘制 ====================

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        drawBoard(g);
        drawPieces(g);
        drawLastMoveMark(g);
        drawWinHighlight(g);
        drawHoverPreview(g);
    }

    private void drawBoard(Graphics2D g) {
        // 木纹渐变背景
        GradientPaint wood = new GradientPaint(0, 0, new Color(218, 175, 85),
                BOARD_PX, BOARD_PX, new Color(185, 135, 50));
        g.setPaint(wood);
        g.fillRect(0, 0, BOARD_PX, BOARD_PX);

        // 木纹细线
        g.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i < 80; i++) {
            g.setColor(new Color(160, 120, 40, 15 + (i % 20)));
            int y = i * 8 + 3;
            g.drawLine(0, y, BOARD_PX, y + (i % 5) - 2);
        }

        // 网格线
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(new Color(80, 55, 25));
        for (int i = 0; i < SIZE; i++) {
            int pos = MARGIN + i * CELL;
            g.drawLine(MARGIN, pos, MARGIN + (SIZE - 1) * CELL, pos);
            g.drawLine(pos, MARGIN, pos, MARGIN + (SIZE - 1) * CELL);
        }

        // 星位
        g.setColor(new Color(80, 55, 25));
        for (int[] s : STARS) {
            int sx = MARGIN + s[1] * CELL;
            int sy = MARGIN + s[0] * CELL;
            g.fillOval(sx - 4, sy - 4, 8, 8);
        }

        // 坐标标注
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(90, 65, 30));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < SIZE; i++) {
            String col = String.valueOf((char) ('A' + i));
            int tx = MARGIN + i * CELL - fm.stringWidth(col) / 2;
            g.drawString(col, tx, MARGIN - 12);
            g.drawString(col, tx, MARGIN + (SIZE - 1) * CELL + 22);
            String row = String.valueOf(SIZE - i);
            int ty = MARGIN + i * CELL + fm.getAscent() / 2 - 1;
            g.drawString(row, MARGIN - fm.stringWidth(row) - 8, ty);
            g.drawString(row, MARGIN + (SIZE - 1) * CELL + 12, ty);
        }
    }

    private void drawPieces(Graphics2D g) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (state.board[r][c] != GameState.EMPTY) {
                    drawPiece(g, r, c, state.board[r][c], false);
                }
            }
        }
    }

    private void drawPiece(Graphics2D g, int r, int c, int player, boolean ghost) {
        int cx = MARGIN + c * CELL;
        int cy = MARGIN + r * CELL;
        float alpha = ghost ? 0.35f : 1.0f;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // 阴影
        if (!ghost) {
            g.setColor(new Color(0, 0, 0, 40));
            g.fillOval(cx - PIECE_R + 2, cy - PIECE_R + 3, PIECE_R * 2, PIECE_R * 2);
        }

        // 棋子主体渐变
        if (player == GameState.BLACK) {
            g.setPaint(new GradientPaint(
                    cx - PIECE_R, cy - PIECE_R, new Color(85, 85, 85),
                    cx + PIECE_R * 0.6f, cy + PIECE_R * 0.6f, new Color(10, 10, 10)));
        } else {
            g.setPaint(new GradientPaint(
                    cx - PIECE_R, cy - PIECE_R, new Color(255, 255, 255),
                    cx + PIECE_R * 0.7f, cy + PIECE_R * 0.7f, new Color(175, 175, 175)));
        }
        g.fillOval(cx - PIECE_R, cy - PIECE_R, PIECE_R * 2, PIECE_R * 2);

        // 白棋边框
        if (player == GameState.WHITE) {
            g.setColor(new Color(110, 110, 110, (int) (alpha * 160)));
            g.setStroke(new BasicStroke(1.0f));
            g.drawOval(cx - PIECE_R, cy - PIECE_R, PIECE_R * 2, PIECE_R * 2);
        }

        // 高光
        g.setColor(new Color(255, 255, 255, (int) (alpha * (player == GameState.BLACK ? 55 : 120))));
        g.fillOval(cx - (int) (PIECE_R * 0.45f), cy - (int) (PIECE_R * 0.45f),
                (int) (PIECE_R * 0.45f), (int) (PIECE_R * 0.45f));

        g.setComposite(old);
    }

    private void drawLastMoveMark(Graphics2D g) {
        if (!state.history.isEmpty() && !state.gameOver) {
            int[] last = state.history.get(state.history.size() - 1);
            int cx = MARGIN + last[1] * CELL;
            int cy = MARGIN + last[0] * CELL;
            g.setColor(new Color(233, 69, 96));
            g.fillOval(cx - 4, cy - 4, 8, 8);
        }
    }

    private void drawWinHighlight(Graphics2D g) {
        int[] wc = state.winCells;
        if (wc == null) {
            return;
        }
        int n = wc.length / 2;
        if (n < 5) {
            return;
        }

        // 找出连线两端
        int minR = wc[0], minC = wc[1], maxR = wc[0], maxC = wc[1];
        for (int i = 1; i < n; i++) {
            if (wc[i * 2] < minR || (wc[i * 2] == minR && wc[i * 2 + 1] < minC)) {
                minR = wc[i * 2];
                minC = wc[i * 2 + 1];
            }
            if (wc[i * 2] > maxR || (wc[i * 2] == maxR && wc[i * 2 + 1] > maxC)) {
                maxR = wc[i * 2];
                maxC = wc[i * 2 + 1];
            }
        }
        int x1 = MARGIN + minC * CELL;
        int y1 = MARGIN + minR * CELL;
        int x2 = MARGIN + maxC * CELL;
        int y2 = MARGIN + maxR * CELL;

        // 连线高亮
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setPaint(new GradientPaint(x1, y1, new Color(245, 197, 24), x2, y2, new Color(233, 69, 96)));
        g.draw(new Line2D.Float(x1, y1, x2, y2));

        // 获胜棋子光圈
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(new Color(245, 197, 24, 200));
        for (int i = 0; i < n; i++) {
            int cx = MARGIN + wc[i * 2 + 1] * CELL;
            int cy = MARGIN + wc[i * 2] * CELL;
            g.drawOval(cx - PIECE_R - 3, cy - PIECE_R - 3, (PIECE_R + 3) * 2, (PIECE_R + 3) * 2);
        }
    }

    private void drawHoverPreview(Graphics2D g) {
        if (hoverR >= 0 && hoverC >= 0 && !state.gameOver && !aiThinking
                && state.board[hoverR][hoverC] == GameState.EMPTY) {
            drawPiece(g, hoverR, hoverC, state.currentPlayer, true);
        }
    }

    // ==================== 鼠标处理 ====================

    /** 屏幕坐标 → 棋盘网格坐标 */
    private static int[] toGrid(int mouseX, int mouseY) {
        int c = Math.round((float) (mouseX - MARGIN) / CELL);
        int r = Math.round((float) (mouseY - MARGIN) / CELL);
        if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) {
            return null;
        }
        return new int[]{r, c};
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (state.gameOver || aiThinking) {
                return;
            }
            if (vsAI && state.currentPlayer == aiPlayer) {
                return;
            }

            int[] pos = toGrid(e.getX(), e.getY());
            if (pos == null) {
                return;
            }

            if (state.place(pos[0], pos[1])) {
                repaint();
                if (controlPanel != null) {
                    controlPanel.refreshUI();
                    if (state.gameOver) {
                        controlPanel.showResultDialog();
                        return;
                    }
                }

                // AI 落子（后台线程计算，不阻塞 EDT）
                if (!state.gameOver && vsAI) {
                    aiThinking = true;
                    repaint();
                    if (controlPanel != null) {
                        controlPanel.refreshUI();
                    }
                    new Thread(() -> {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        int[] move = AIPlayer.findBestMove(state.board, aiPlayer);
                        SwingUtilities.invokeLater(() -> {
                            state.place(move[0], move[1]);
                            aiThinking = false;
                            repaint();
                            if (controlPanel != null) {
                                controlPanel.refreshUI();
                                if (state.gameOver) {
                                    controlPanel.showResultDialog();
                                }
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
            if (nr != hoverR || nc != hoverC) {
                hoverR = nr;
                hoverC = nc;
                repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            hoverR = hoverC = -1;
            repaint();
        }
    }

    // ==================== 公共操作 ====================

    public void restart() {
        state.reset();
        hoverR = hoverC = -1;
        aiThinking = false;
        repaint();
    }

    public void undo() {
        if (state.gameOver) {
            state.gameOver = false;
            state.winCells = null;
            state.draw = false;
        }
        if (vsAI && state.history.size() >= 2) {
            state.undo();
            state.undo();
        } else if (!state.history.isEmpty()) {
            state.undo();
        }
        repaint();
    }

    public void setVsAI(boolean ai) {
        this.vsAI = ai;
        restart();
    }
}
