package com.gomoku.ui;

import com.gomoku.model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 控制面板：现代极简风格，圆角按钮，清晰信息层级。
 */
public class ControlPanel extends JPanel {

    private final BoardPanel boardPanel;
    private final JLabel turnLabel;
    private final JLabel scoreLabel;
    private final JButton undoBtn;
    private final JButton restartBtn;
    private final JToggleButton aiToggle;

    // ---- 颜色系统 ----
    private static final Color PANEL_BG     = new Color(22, 22, 40);
    private static final Color TEXT_PRIMARY = new Color(230, 228, 222);
    private static final Color TEXT_SECOND  = new Color(170, 165, 155);
    private static final Color ACCENT       = new Color(212, 168, 83);
    private static final Color BTN_BG       = new Color(45, 45, 65);
    private static final Color BTN_HOVER    = new Color(58, 58, 82);
    private static final Color TOGGLE_ON    = new Color(212, 168, 83, 35);
    private static final Color RESTART_BG   = new Color(185, 65, 65);
    private static final Color RESTART_HOVER= new Color(205, 75, 75);
    private static final Color BORDER_TOP   = new Color(60, 55, 45);

    public ControlPanel(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_TOP),
                new EmptyBorder(10, 20, 10, 20)));

        // ---- 左侧：模式切换 + 回合信息 ----
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftPanel.setOpaque(false);

        // 人机 / 双人 切换
        aiToggle = new JToggleButton("人机对弈");
        aiToggle.setSelected(true);
        styleToggle(aiToggle);
        aiToggle.addActionListener(e -> {
            boardPanel.setVsAI(aiToggle.isSelected());
            aiToggle.setText(aiToggle.isSelected() ? "人机对弈" : "双人对弈");
            refreshUI();
        });
        leftPanel.add(aiToggle);

        // 分隔
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 22));
        sep.setForeground(new Color(70, 65, 55));
        leftPanel.add(sep);

        // 回合
        turnLabel = new JLabel("●  黑棋落子");
        turnLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        turnLabel.setForeground(TEXT_PRIMARY);
        leftPanel.add(turnLabel);

        // 比分
        scoreLabel = new JLabel("黑 0 : 0 白");
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        scoreLabel.setForeground(TEXT_SECOND);
        leftPanel.add(scoreLabel);

        // ---- 右侧：操作按钮 ----
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        undoBtn = makePillButton("↩ 悔棋", BTN_BG, BTN_HOVER, TEXT_PRIMARY);
        undoBtn.addActionListener(e -> { boardPanel.undo(); refreshUI(); });
        rightPanel.add(undoBtn);

        restartBtn = makePillButton("↻ 重新开始", RESTART_BG, RESTART_HOVER, Color.WHITE);
        restartBtn.addActionListener(e -> { boardPanel.restart(); refreshUI(); });
        rightPanel.add(restartBtn);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        refreshUI();
    }

    // ---- 样式工具 ----

    private void styleToggle(JToggleButton btn) {
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(TOGGLE_ON);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 75, 65), 1),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        btn.addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(TOGGLE_ON);
            } else {
                btn.setBackground(BTN_BG);
            }
        });
    }

    private JButton makePillButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 34));

        // 圆角通过自定义绘制
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();

                int w = b.getWidth(), h = b.getHeight();
                Color fill = bg;
                if (model.isPressed()) fill = fill.darker();
                else if (model.isRollover()) fill = hoverBg;

                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);

                // 文字
                FontMetrics fm = g2.getFontMetrics();
                Rectangle r = new Rectangle(0, 0, w, h);
                SwingUtilities.layoutCompoundLabel(fm, b.getText(), null,
                        b.getVerticalAlignment(), b.getHorizontalAlignment(),
                        b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                        r, new Rectangle(), r, b.getIconTextGap());
                g2.setColor(fg);
                g2.drawString(b.getText(), r.x, r.y + fm.getAscent());
            }
        });

        return btn;
    }

    // ---- 对话框 ----

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
            String symbol = (winner == GameState.BLACK) ? "●" : "○";
            title = symbol + " " + wn + " 获胜";
            message = wn + " 取得胜利\n黑 " + s.score[1] + " : " + s.score[2] + " 白";
            messageType = JOptionPane.PLAIN_MESSAGE;
        }

        Object[] options = {"再来一局", "关闭"};
        int choice = JOptionPane.showOptionDialog(this, message, title,
                JOptionPane.YES_NO_OPTION, messageType, null, options, options[0]);

        if (choice == 0) { boardPanel.restart(); refreshUI(); }
    }

    // ---- 刷新 ----

    public void refreshUI() {
        GameState s = boardPanel.getState();
        if (s.gameOver) {
            if (s.draw) {
                turnLabel.setText("  平局");
                turnLabel.setForeground(TEXT_SECOND);
            } else {
                int winner = s.history.get(s.history.size() - 1)[2];
                turnLabel.setText((winner == GameState.BLACK ? "● " : "○ ")
                        + (winner == GameState.BLACK ? "黑棋" : "白棋") + " 获胜！");
                turnLabel.setForeground(ACCENT);
            }
        } else {
            String prefix = s.currentPlayer == GameState.BLACK ? "●  " : "○  ";
            String name = s.currentPlayer == GameState.BLACK ? "黑棋" : "白棋";
            if (boardPanel.isVsAI() && s.currentPlayer == boardPanel.getAiPlayer()) {
                turnLabel.setText(prefix + name + "  思考中…");
            } else {
                turnLabel.setText(prefix + name + "  落子");
            }
            turnLabel.setForeground(s.currentPlayer == GameState.BLACK
                    ? new Color(210, 208, 200) : new Color(245, 243, 238));
        }
        scoreLabel.setText("黑 " + s.score[1] + "  :  " + s.score[2] + " 白");
        undoBtn.setEnabled(!s.history.isEmpty());
    }
}
