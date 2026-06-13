package com.gomoku.ui;

import com.gomoku.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 五子棋 — 程序入口，深色主题窗口。
 */
public class Main {

    public static void main(String[] args) {
        // 全局字体渲染优化
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            // Flat 风格 LookAndFeel
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored2) {}
            }

            // Nimbus 全局调色
            UIManager.put("control", new Color(30, 30, 48));
            UIManager.put("nimbusBase", new Color(30, 30, 48));
            UIManager.put("nimbusFocus", new Color(212, 168, 83));
            UIManager.put("nimbusLightBackground", new Color(40, 40, 58));
            UIManager.put("text", new Color(230, 228, 222));
            UIManager.put("OptionPane.background", new Color(30, 30, 48));
            UIManager.put("Panel.background", new Color(30, 30, 48));
            UIManager.put("OptionPane.messageForeground", new Color(230, 228, 222));

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

            // 整体背景
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
