package com.gomoku.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏状态管理：棋盘、落子、悔棋、胜负判定。
 */
public class GameState {

    public static final int SIZE = 15;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    public int[][] board = new int[SIZE][SIZE];
    public int currentPlayer = BLACK;
    public boolean gameOver;
    public boolean draw;
    public List<int[]> history = new ArrayList<>();
    /** [1]=黑胜次数, [2]=白胜次数 */
    public int[] score = {0, 0, 0};
    /** 获胜连线坐标，格式 [r0,c0, r1,c1, ...]；未获胜时为 null */
    public int[] winCells;

    /** 重置为初始状态 */
    public void reset() {
        board = new int[SIZE][SIZE];
        currentPlayer = BLACK;
        gameOver = false;
        draw = false;
        history.clear();
        winCells = null;
    }

    /**
     * 在指定位置落子。
     *
     * @return true 表示落子成功，false 表示非法落子
     */
    public boolean place(int r, int c) {
        if (gameOver || r < 0 || r >= SIZE || c < 0 || c >= SIZE || board[r][c] != EMPTY) {
            return false;
        }
        board[r][c] = currentPlayer;
        history.add(new int[]{r, c, currentPlayer});

        int[] win = checkWin(r, c, currentPlayer);
        if (win != null) {
            winCells = win;
            gameOver = true;
            score[currentPlayer]++;
        } else if (history.size() == SIZE * SIZE) {
            gameOver = true;
            draw = true;
        } else {
            currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
        }
        return true;
    }

    /** 悔棋，回退最近一步 */
    public boolean undo() {
        if (history.isEmpty() || gameOver) {
            return false;
        }
        int[] last = history.remove(history.size() - 1);
        board[last[0]][last[1]] = EMPTY;
        currentPlayer = last[2];
        return true;
    }

    /**
     * 检查 (r,c) 落子后是否形成五连。
     *
     * @return 五连坐标数组，未形成则返回 null
     */
    public int[] checkWin(int r, int c, int player) {
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] d : dirs) {
            int dr = d[0];
            int dc = d[1];
            int count = 1;
            List<int[]> cells = new ArrayList<>();
            cells.add(new int[]{r, c});

            // 正方向扫描
            for (int i = 1; i < 5; i++) {
                int nr = r + dr * i;
                int nc = c + dc * i;
                if (nr < 0 || nr >= SIZE || nc < 0 || nc >= SIZE || board[nr][nc] != player) {
                    break;
                }
                cells.add(new int[]{nr, nc});
                count++;
            }
            // 反方向扫描
            for (int i = 1; i < 5; i++) {
                int nr = r - dr * i;
                int nc = c - dc * i;
                if (nr < 0 || nr >= SIZE || nc < 0 || nc >= SIZE || board[nr][nc] != player) {
                    break;
                }
                cells.add(0, new int[]{nr, nc});
                count++;
            }

            if (count >= 5) {
                int[] result = new int[count * 2];
                for (int i = 0; i < count; i++) {
                    result[i * 2] = cells.get(i)[0];
                    result[i * 2 + 1] = cells.get(i)[1];
                }
                return result;
            }
        }
        return null;
    }
}
