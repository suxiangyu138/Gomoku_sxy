package com.gomoku.ui;

import com.gomoku.model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 控制面板：现代 pill 切换、圆角按钮、清晰信息层级。
 */
public class ControlPanel extends JPanel {

    private final BoardPanel boardPanel;
    private final JLabel turnLabel;
    private final JLabel scoreLabel;
    private final JButton undoBtn;
    private final JButton restartBtn;

    // 自定义 pill 切换
    private JPanel modeSwitch;
    private JLabel aiLabel;
    private JLabel pvpLabel;
    private JPanel sliderKnob;
    private boolean modeIsAI = true;

    // ---- 颜色 ----
    private static final Color PANEL_BG     = new Color(20, 20, 38);
    private static final Color TEXT_PRIMARY = new Color(232, 230, 225);
    private static final Color TEXT_SECOND  = new Color(168, 163, 150);
    private static final Color ACCENT       = new Color(218, 175, 85);
    private static final Color BTN_BG       = new Color(48, 48, 68);
    private static final Color BTN_HOVER    = new Color(65, 65, 90);
    private static final Color RESTART_BG   = new Color(185, 62, 62);
    private static final Color RESTART_HOVER= new Color(210, 78, 78);
    private static final Color TRACK_BG     = new Color(38, 38, 58);
    private static final Color KNOB_COLOR   = new Color(218, 175, 85);

    public ControlPanel(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
        setLayout(new BorderLayout(16, 0));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 65)),
                new EmptyBorder(14, 24, 14, 24)));

        // ---- 左侧 ----
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
        leftPanel.setOpaque(false);

        // 自定义 pill 模式切换
        modeSwitch = createModeSwitch();
        leftPanel.add(modeSwitch);

        turnLabel = new JLabel("●  黑棋  落子");
        turnLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        turnLabel.setForeground(TEXT_PRIMARY);
        leftPanel.add(turnLabel);

        scoreLabel = new JLabel("黑 0 : 0 白");
        scoreLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        scoreLabel.setForeground(TEXT_SECOND);
        leftPanel.add(scoreLabel);

        // ---- 右侧 ----
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        rightPanel.setOpaque(false);

        undoBtn = makeButton("↩  悔棋", BTN_BG, BTN_HOVER, TEXT_PRIMARY);
        undoBtn.addActionListener(e -> { boardPanel.undo(); refreshUI(); });
        rightPanel.add(undoBtn);

        restartBtn = makeButton("↻  重新开始", RESTART_BG, RESTART_HOVER, Color.WHITE);
        restartBtn.addActionListener(e -> { boardPanel.restart(); refreshUI(); });
        rightPanel.add(restartBtn);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        refreshUI();
    }

    // ==================== 自定义 Pill 切换 ====================

    private JPanel createModeSwitch() {
        JPanel track = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TRACK_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 22, 22));
            }
        };
        track.setPreferredSize(new Dimension(184, 38));
        track.setOpaque(false);
        track.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 滑块
        sliderKnob = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(KNOB_COLOR);
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 19, 19));
            }
        };
        sliderKnob.setOpaque(false);
        sliderKnob.setBounds(3, 3, 88, 32);

        // 标签
        aiLabel = new JLabel("人机", SwingConstants.CENTER);
        aiLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        aiLabel.setForeground(new Color(30, 30, 45));
        aiLabel.setBounds(3, 3, 88, 32);
        aiLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pvpLabel = new JLabel("双人", SwingConstants.CENTER);
        pvpLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        pvpLabel.setForeground(new Color(180, 175, 165));
        pvpLabel.setBounds(93, 3, 88, 32);
        pvpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 点击事件
        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean clickedAI = e.getX() < track.getWidth() / 2;
                if (clickedAI != modeIsAI) {
                    modeIsAI = clickedAI;
                    animateSlider();
                    boardPanel.setVsAI(modeIsAI);
                    refreshUI();
                }
            }
        };
        track.addMouseListener(clickHandler);
        aiLabel.addMouseListener(clickHandler);
        pvpLabel.addMouseListener(clickHandler);

        track.add(sliderKnob);
        track.add(aiLabel);
        track.add(pvpLabel);

        return track;
    }

    private void animateSlider() {
        int targetX = modeIsAI ? 3 : 93;
        int startX = sliderKnob.getX();
        Timer anim = new Timer(10, null);
        anim.addActionListener(e -> {
            int current = sliderKnob.getX();
            int next = current + (targetX - current) / 3;
            if (Math.abs(next - targetX) <= 1) {
                sliderKnob.setLocation(targetX, 3);
                anim.stop();
            } else {
                sliderKnob.setLocation(next, 3);
            }
            // 更新文字颜色
            float progress = (float)(sliderKnob.getX() - 3) / 90f;
            aiLabel.setForeground(lerpColor(new Color(30, 30, 45), new Color(180, 175, 165), progress));
            pvpLabel.setForeground(lerpColor(new Color(180, 175, 165), new Color(30, 30, 45), progress));
        });
        anim.start();
    }

    private static Color lerpColor(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    // ==================== 按钮 ====================

    private JButton makeButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = b.getWidth(), h = b.getHeight();
                ButtonModel m = b.getModel();
                Color fill = bg;
                if (m.isPressed()) fill = bg.darker();
                else if (m.isRollover()) fill = hoverBg;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 22, 22);
                g2.dispose();
                super.paint(g, c);
            }
            @Override
            protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                ButtonModel m = b.getModel();
                FontMetrics fm = g2.getFontMetrics(b.getFont());
                int tw = fm.stringWidth(b.getText());
                int th = fm.getHeight();
                int x = (c.getWidth() - tw) / 2;
                int y = (c.getHeight() - th) / 2 + fm.getAscent();
                g2.setColor(m.isEnabled() ? b.getForeground()
                        : new Color(b.getForeground().getRed(), b.getForeground().getGreen(),
                                    b.getForeground().getBlue(), 80));
                g2.drawString(b.getText(), x, y);
            }
        });

        return btn;
    }

    // ==================== 对话框 ====================

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

    // ==================== 刷新 ====================

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
            String dot = s.currentPlayer == GameState.BLACK ? "●  " : "○  ";
            String name = s.currentPlayer == GameState.BLACK ? "黑棋" : "白棋";
            if (boardPanel.isVsAI() && s.currentPlayer == boardPanel.getAiPlayer()) {
                turnLabel.setText(dot + name + "  思考中…");
            } else {
                turnLabel.setText(dot + name + "  落子");
            }
            turnLabel.setForeground(s.currentPlayer == GameState.BLACK
                    ? new Color(210, 208, 200) : new Color(248, 246, 240));
        }
        scoreLabel.setText("黑 " + s.score[1] + " : " + s.score[2] + " 白");
        undoBtn.setEnabled(!s.history.isEmpty());
    }
}
