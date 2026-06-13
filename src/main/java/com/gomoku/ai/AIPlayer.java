package com.gomoku.ai;

import com.gomoku.model.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 玩家：基于棋型评分的启发式搜索。
 *
 * <p>对每个候选空位计算攻击分（为己方连子）和防守分（阻止对方连子），
 * 加权求和后选择最高分位置落子。</p>
 */
public final class AIPlayer {

    private static final int SIZE = GameState.SIZE;

    private AIPlayer() {
        // 工具类，禁止实例化
    }

    /**
     * 寻找最佳落子位置。
     *
     * @param board   当前棋盘状态
     * @param aiPlayer AI 执子颜色
     * @return [row, col] 最佳落子坐标
     */
    public static int[] findBestMove(int[][] board, int aiPlayer) {
        int opp = (aiPlayer == GameState.BLACK) ? GameState.WHITE : GameState.BLACK;

        // 棋盘为空时下天元
        if (isBoardEmpty(board)) {
            return new int[]{7, 7};
        }

        int bestScore = -1;
        List<int[]> candidates = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != GameState.EMPTY) {
                    continue;
                }
                if (!isNearStone(board, r, c)) {
                    continue;
                }

                int attack = evaluatePosition(board, r, c, aiPlayer);
                int defend = evaluatePosition(board, r, c, opp);
                int total = attack * 11 / 10 + defend;
                // 靠近中心加分
                int centerDist = Math.abs(r - 7) + Math.abs(c - 7);
                total += Math.max(0, 14 - centerDist);

                if (total > bestScore) {
                    bestScore = total;
                    candidates.clear();
                    candidates.add(new int[]{r, c});
                } else if (total == bestScore) {
                    candidates.add(new int[]{r, c});
                }
            }
        }

        if (candidates.isEmpty()) {
            return new int[]{7, 7};
        }
        return candidates.get((int) (Math.random() * candidates.size()));
    }

    private static boolean isBoardEmpty(int[][] board) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != GameState.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 检查 (r,c) 周围 2 格内是否有棋子 */
    private static boolean isNearStone(int[][] board, int r, int c) {
        for (int dr = -2; dr <= 2; dr++) {
            for (int dc = -2; dc <= 2; dc++) {
                int nr = r + dr;
                int nc = c + dc;
                if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE
                        && board[nr][nc] != GameState.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 评估某玩家在 (r,c) 落子的综合得分 */
    private static int evaluatePosition(int[][] board, int r, int c, int player) {
        int opp = (player == GameState.BLACK) ? GameState.WHITE : GameState.BLACK;
        int total = 0;
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] d : dirs) {
            int[] line = new int[9];
            for (int i = -4; i <= 4; i++) {
                int nr = r + d[0] * i;
                int nc = c + d[1] * i;
                if (nr < 0 || nr >= SIZE || nc < 0 || nc >= SIZE) {
                    line[i + 4] = opp; // 边界视为对方棋子阻挡
                } else {
                    line[i + 4] = board[nr][nc];
                }
            }
            total += evaluateLine(line, player);
        }
        return total;
    }

    /** 对一条线上的棋型打分 */
    private static int evaluateLine(int[] line, int player) {
        int opp = (player == GameState.BLACK) ? GameState.WHITE : GameState.BLACK;
        int score = 0;
        for (int start = 0; start < line.length; start++) {
            if (line[start] == opp || line[start] == GameState.EMPTY) {
                continue;
            }
            int count = 0;
            int openEnds = 0;

            if (start > 0 && line[start - 1] == GameState.EMPTY) {
                openEnds++;
            }

            int i = start;
            while (i < line.length && line[i] == player) {
                count++;
                i++;
            }

            if (i < line.length && line[i] == GameState.EMPTY) {
                openEnds++;
            }

            // 棋型评分
            if (count >= 5) {
                score += 100_000;
            } else if (count == 4 && openEnds == 2) {
                score += 10_000;
            } else if (count == 4 && openEnds == 1) {
                score += 1_000;
            } else if (count == 3 && openEnds == 2) {
                score += 1_000;
            } else if (count == 3 && openEnds == 1) {
                score += 100;
            } else if (count == 2 && openEnds == 2) {
                score += 100;
            } else if (count == 2 && openEnds == 1) {
                score += 10;
            } else if (count == 1 && openEnds == 2) {
                score += 10;
            }
            start = i - 1;
        }
        return score;
    }
}
