package com.gomoku.ui;

import com.gomoku.model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 控制面板：显示回合、比分、操作按钮。
 */
public class ControlPanel extends JPanel {

    private final BoardPanel boardPanel;
    private final JLabel turnLabel;
    private final JLabel scoreLabel;
    private final JButton undoBtn;
    private final JButton restartBtn;
    private final JToggleButton aiToggle;

    public ControlPanel(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
        setLayout(new FlowLayout(FlowLayout.CENTER, 16, 10));
        setBackground(new Color(30, 30, 50));
        setBorder(new EmptyBorder(4, 10, 4, 10));

        // 人机/双人切换
        aiToggle = new JToggleButton("人机对弈");
        aiToggle.setSelected(true);
        styleToggleButton(aiToggle);
        aiToggle.addActionListener(e -> {
            boardPanel.setVsAI(aiToggle.isSelected());
            aiToggle.setText(aiToggle.isSelected() ? "人机对弈" : "双人对弈");
            refreshUI();
        });

        // 回合显示
        turnLabel = new JLabel("● 黑棋落子");
        turnLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        turnLabel.setForeground(new Color(230, 230, 230));

        // 比分
        scoreLabel = new JLabel("黑 0 : 0 白");
        scoreLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        scoreLabel.setForeground(new Color(200, 200, 200));

        // 悔棋
        undoBtn = makeButton("悔棋");
        undoBtn.addActionListener(e -> {
            boardPanel.undo();
            refreshUI();
        });

        // 重新开始
        restartBtn = makeButton("重新开始");
        restartBtn.setBackground(new Color(200, 60, 70));
        restartBtn.addActionListener(e -> {
            boardPanel.restart();
            refreshUI();
        });

        add(aiToggle);
        add(Box.createHorizontalStrut(10));
        add(turnLabel);
        add(scoreLabel);
        add(Box.createHorizontalStrut(10));
        add(undoBtn);
        add(restartBtn);

        refreshUI();
    }

    private void styleToggleButton(JToggleButton btn) {
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 60, 90));
        btn.setFocusPainted(false);
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 60, 100));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 32));
        return btn;
    }

    /** 弹出游戏结果对话框（胜利/平局） */
    public void showResultDialog() {
        GameState s = boardPanel.getState();
        if (!s.gameOver) {
            return;
        }

        String title;
        String message;
        int messageType;

        if (s.draw) {
            title = "平局";
            message = "棋盘已满，双方平局！\n\n是否重新开始？";
            messageType = JOptionPane.INFORMATION_MESSAGE;
        } else {
            int winner = s.history.get(s.history.size() - 1)[2];
            String winnerName = (winner == GameState.BLACK) ? "黑棋 ●" : "白棋 ○";
            title = winnerName + " 获胜！";
            message = "恭喜 " + winnerName + " 取得胜利！\n\n"
                    + "比分：黑 " + s.score[1] + " : " + s.score[2] + " 白\n\n"
                    + "是否重新开始？";
            messageType = JOptionPane.PLAIN_MESSAGE;
        }

        int choice = JOptionPane.showConfirmDialog(
                this, message, title,
                JOptionPane.YES_NO_OPTION, messageType);

        if (choice == JOptionPane.YES_OPTION) {
            boardPanel.restart();
            refreshUI();
        }
    }

    public void refreshUI() {
        GameState s = boardPanel.getState();
        if (s.gameOver) {
            if (s.draw) {
                turnLabel.setText("  平局");
                turnLabel.setForeground(new Color(200, 200, 200));
            } else {
                int winner = s.history.get(s.history.size() - 1)[2];
                turnLabel.setText((winner == GameState.BLACK ? "● 黑棋" : "○ 白棋") + " 获胜！");
                turnLabel.setForeground(new Color(245, 197, 24));
            }
        } else {
            String prefix = s.currentPlayer == GameState.BLACK ? "● 黑棋" : "○ 白棋";
            if (boardPanel.isVsAI() && s.currentPlayer == boardPanel.getAiPlayer()) {
                turnLabel.setText(prefix + " 思考中...");
            } else {
                turnLabel.setText(prefix + " 落子");
            }
            turnLabel.setForeground(s.currentPlayer == GameState.BLACK
                    ? new Color(200, 200, 200) : new Color(255, 255, 255));
        }
        scoreLabel.setText(String.format("黑 %d : %d 白", s.score[1], s.score[2]));
        undoBtn.setEnabled(!s.history.isEmpty());
    }
}
