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
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // 回退到默认 LookAndFeel
            }

            GameState state = new GameState();
            BoardPanel boardPanel = new BoardPanel(state);
            ControlPanel controlPanel = new ControlPanel(boardPanel);
            boardPanel.setControlPanel(controlPanel);

            JFrame frame = new JFrame("五子棋");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.dispose();
                    System.exit(0);
                }
            });
            frame.setResizable(false);
            frame.setLayout(new BorderLayout());
            frame.add(controlPanel, BorderLayout.NORTH);
            frame.add(boardPanel, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
