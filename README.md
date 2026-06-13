# 五子棋 (Gomoku)

> Java Swing 双人对战 / 人机对弈五子棋游戏，Maven 构建

## 项目概述

基于 Java Swing 开发的经典五子棋（Gomoku）游戏。在 15×15 棋盘上，黑白双方轮流落子，率先在横、竖、斜任意方向连成五子者获胜。支持**人机对弈**（启发式 AI）和**双人对战**两种模式。

## 技术栈

| 技术 | 说明 |
|------|------|
| Java | JDK 24 |
| Swing / AWT | 棋盘绘制、鼠标事件处理 |
| Maven | 项目构建与依赖管理 |
| Java 2D | Graphics 绘图、棋子渲染 |
| JUnit 5 | 单元测试 |

## 项目结构

```
Gomoku_sxy/
├── .gitignore
├── pom.xml
├── README.md
├── src/
│   ├── main/java/com/gomoku/
│   │   ├── model/
│   │   │   └── GameState.java       # 游戏状态 & 胜负判定
│   │   ├── ai/
│   │   │   └── AIPlayer.java        # AI 启发式搜索
│   │   └── ui/
│   │       ├── Main.java            # 程序入口
│   │       ├── BoardPanel.java      # 棋盘渲染 & 鼠标输入
│   │       └── ControlPanel.java    # 控制面板 UI
│   └── test/java/com/gomoku/       # 单元测试
└── target/                          # Maven 构建输出（gitignored）
```

## 游戏规则

- 棋盘：15 × 15 交叉点
- 执黑先行，双方交替落子
- 横、竖、斜任意方向先连成五子者获胜
- 棋盘满而未分胜负则为平局

## 功能特性

- 15×15 标准棋盘绘制（木纹背景 + 星位标记 + 坐标标注）
- 人机对弈 / 双人对战一键切换
- 鼠标悬停预览落子位置
- 最后落子标记 + 获胜连线高亮
- 悔棋、重新开始、比分统计
- AI 基于棋型评分的启发式搜索

## 快速开始

```bash
# 编译
mvn compile

# 运行测试
mvn test

# 打包
mvn package

# 运行
java -jar target/gomoku-1.0-SNAPSHOT.jar
```

## AI 算法

采用启发式棋型评分策略：
- 对每个候选空位评估攻击分（为己方创造连子）和防守分（阻止对方连子）
- 评分权重：活四 10000，冲四/活三 1000，活二 100，活一 10
- 靠近中心位置额外加分
- 仅搜索已有棋子周围 2 格范围，提升效率

## License

MIT
