package com.gomoku.ui;

import com.gomoku.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 五子棋 — 程序入口。
 */
public class Main {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            // 使用系统默认 LookAndFeel（Windows 上好兼容中文）
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // 全局背景（OptionPane 等弹窗）
            UIManager.put("OptionPane.background", new Color(28, 28, 46));
            UIManager.put("Panel.background", new Color(28, 28, 46));
            UIManager.put("OptionPane.messageForeground", new Color(230, 228, 222));
            UIManager.put("OptionPane.messageFont", new Font("Microsoft YaHei", Font.PLAIN, 13));
            UIManager.put("Button.font", new Font("Microsoft YaHei", Font.BOLD, 13));

            GameState state = new GameState();
            BoardPanel boardPanel = new BoardPanel(state);
            ControlPanel controlPanel = new ControlPanel(boardPanel);
            boardPanel.setControlPanel(controlPanel);

            JFrame frame = new JFrame("五子棋  ·  Gomoku");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.dispose();
                    System.exit(0);
                }
            });
            frame.setResizable(false);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(26, 26, 46));
            root.add(controlPanel, BorderLayout.NORTH);
            root.add(boardPanel, BorderLayout.CENTER);

            frame.setContentPane(root);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
