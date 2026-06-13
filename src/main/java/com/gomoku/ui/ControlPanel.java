package com.gomoku.ui;

import com.gomoku.model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 控制面板：标准 Swing 组件，稳定可靠的布局和文字渲染。
 */
public class ControlPanel extends JPanel {

    private final BoardPanel boardPanel;
    private final JLabel turnLabel;
    private final JLabel scoreLabel;
    private final JButton undoBtn;
    private final JButton restartBtn;
    private final JToggleButton aiToggle;

    private static final Color PANEL_BG     = new Color(22, 22, 40);
    private static final Color TEXT_PRIMARY = new Color(230, 228, 222);
    private static final Color TEXT_SECOND  = new Color(170, 165, 155);
    private static final Color ACCENT       = new Color(212, 168, 83);
    private static final Color BTN_BG       = new Color(50, 50, 70);
    private static final Color TOGGLE_ON_BG = new Color(75, 70, 55);
    private static final Color RESTART_BG   = new Color(185, 65, 65);
    private static final Color BORDER_TOP   = new Color(55, 55, 70);

    private static final Font FONT_BOLD   = new Font("Microsoft YaHei", Font.BOLD, 14);
    private static final Font FONT_NORMAL = new Font("Microsoft YaHei", Font.PLAIN, 13);
    private static final Font FONT_BTN    = new Font("Microsoft YaHei", Font.BOLD, 13);
    private static final Font FONT_SCORE  = new Font("Microsoft YaHei", Font.PLAIN, 14);

    public ControlPanel(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
        setLayout(new BorderLayout(16, 0));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_TOP),
                new EmptyBorder(12, 22, 12, 22)));

        // ---- 左侧 ----
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        leftPanel.setOpaque(false);

        aiToggle = new JToggleButton("人机对弈");
        aiToggle.setSelected(true);
        aiToggle.setFont(FONT_BTN);
        aiToggle.setForeground(TEXT_PRIMARY);
        aiToggle.setBackground(TOGGLE_ON_BG);
        aiToggle.setFocusPainted(false);
        aiToggle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 95, 80), 1),
                new EmptyBorder(5, 14, 5, 14)));
        aiToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        aiToggle.addActionListener(e -> {
            boardPanel.setVsAI(aiToggle.isSelected());
            aiToggle.setText(aiToggle.isSelected() ? "人机对弈" : "双人对弈");
            if (aiToggle.isSelected()) {
                aiToggle.setBackground(TOGGLE_ON_BG);
            } else {
                aiToggle.setBackground(BTN_BG);
            }
            refreshUI();
        });
        leftPanel.add(aiToggle);

        turnLabel = new JLabel("●  黑棋  落子");
        turnLabel.setFont(FONT_BOLD);
        turnLabel.setForeground(TEXT_PRIMARY);
        leftPanel.add(turnLabel);

        scoreLabel = new JLabel("黑 0 : 0 白");
        scoreLabel.setFont(FONT_SCORE);
        scoreLabel.setForeground(TEXT_SECOND);
        leftPanel.add(scoreLabel);

        // ---- 右侧 ----
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        rightPanel.setOpaque(false);

        undoBtn = new JButton("↩  悔棋");
        styleButton(undoBtn, BTN_BG, new Color(80, 80, 105), TEXT_PRIMARY);
        undoBtn.addActionListener(e -> { boardPanel.undo(); refreshUI(); });
        rightPanel.add(undoBtn);

        restartBtn = new JButton("↻  重新开始");
        styleButton(restartBtn, RESTART_BG, new Color(210, 85, 85), Color.WHITE);
        restartBtn.addActionListener(e -> { boardPanel.restart(); refreshUI(); });
        rightPanel.add(restartBtn);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        refreshUI();
    }

    /**
     * 标准 JButton 样式——不使用自定义 UI 覆盖，确保文字正常渲染。
     */
    private void styleButton(JButton btn, Color bg, Color hoverBg, Color fg) {
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 18, 7, 18));

        // 通过重写 paintComponent 实现圆角背景
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = b.getWidth(), h = b.getHeight();
                ButtonModel m = b.getModel();
                Color fill = bg;
                if (m.isPressed()) {
                    fill = bg.darker();
                } else if (m.isRollover()) {
                    fill = hoverBg;
                }
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);
                g2.dispose();
                // 让父类绘制文字（位置正确）
                super.paint(g, c);
            }
            @Override
            protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                FontMetrics fm = g2.getFontMetrics(b.getFont());
                int x = (c.getWidth() - fm.stringWidth(b.getText())) / 2;
                int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(b.getForeground());
                g2.drawString(b.getText(), x, y);
            }
        });
    }

    public void showResultDialog() {
        GameState s = boardPanel.getState();
        if (!s.gameOver) return;

        String title, message;
        int messageType;

        if (s.draw) {
            title = "平局";
            message = "棋盘已满，双方平局";
            messageType = JOptionPane.INFORMATION_MESSAGE;
        } else {
            int winner = s.history.get(s.history.size() - 1)[2];
            String wn = (winner == GameState.BLACK) ? "黑棋" : "白棋";
            title = (winner == GameState.BLACK ? "● " : "○ ") + wn + " 获胜";
            message = wn + " 取得胜利\n\n黑 " + s.score[1] + " : " + s.score[2] + " 白";
            messageType = JOptionPane.PLAIN_MESSAGE;
        }

        Object[] options = {"再来一局", "关闭"};
        int choice = JOptionPane.showOptionDialog(this, message, title,
                JOptionPane.YES_NO_OPTION, messageType, null, options, options[0]);

        if (choice == 0) { boardPanel.restart(); refreshUI(); }
    }

    public void refreshUI() {
        GameState s = boardPanel.getState();
        if (s.gameOver) {
            if (s.draw) {
                turnLabel.setText("  平局");
                turnLabel.setForeground(TEXT_SECOND);
            } else {
                int winner = s.history.get(s.history.size() - 1)[2];
                turnLabel.setText((winner == GameState.BLACK ? "●  " : "○  ")
                        + (winner == GameState.BLACK ? "黑棋" : "白棋") + "  获胜！");
                turnLabel.setForeground(ACCENT);
            }
        } else {
            String dotColor = s.currentPlayer == GameState.BLACK ? "●  " : "○  ";
            String name = s.currentPlayer == GameState.BLACK ? "黑棋" : "白棋";
            if (boardPanel.isVsAI() && s.currentPlayer == boardPanel.getAiPlayer()) {
                turnLabel.setText(dotColor + name + "  思考中…");
            } else {
                turnLabel.setText(dotColor + name + "  落子");
            }
            turnLabel.setForeground(s.currentPlayer == GameState.BLACK
                    ? new Color(210, 208, 200) : new Color(245, 243, 238));
        }
        scoreLabel.setText("黑 " + s.score[1] + " : " + s.score[2] + " 白");
        undoBtn.setEnabled(!s.history.isEmpty());
    }
}
