# 五子棋 (Gomoku)

> Java Swing 双人对战 / 人机对弈五子棋游戏，现代深色主题，Maven 构建

## 界面预览

深色主题 × 暖白棋盘，写实径向渐变棋子，iOS 风格 Pill 模式切换。

## 项目概述

基于 Java Swing 开发的经典五子棋（Gomoku）游戏。15×15 棋盘，黑白双方轮流落子，横、竖、斜任意方向先连成五子者获胜。支持**人机对弈**（启发式 AI）和**双人对战**，一键切换。

## 技术栈

| 技术 | 说明 |
|------|------|
| Java | JDK 24 |
| Swing / AWT | 窗口框架、事件处理 |
| Java 2D | RadialGradientPaint 写实棋子、圆角抗锯齿 |
| Maven | 项目构建与依赖管理 |
| JUnit 5 | 单元测试框架 |

## 项目结构

```
Gomoku_sxy/
├── .gitignore
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/gomoku/
    │   ├── model/
    │   │   └── GameState.java       # 游戏状态 & 四方向胜负判定
    │   ├── ai/
    │   │   └── AIPlayer.java        # 棋型评分启发式搜索
    │   └── ui/
    │       ├── Main.java            # 程序入口 & 全局主题
    │       ├── BoardPanel.java      # 棋盘渲染 & 鼠标精准落子
    │       └── ControlPanel.java    # 控制面板 & Pill 模式切换
    └── test/java/com/gomoku/       # 单元测试目录
```

## 功能特性

- **15×15** 标准棋盘，暖白底色 + 星位标记 + 行列坐标标注
- **Pill 滑动切换** — 人机对弈 ⇄ 双人对战，带平滑过渡动画
- **写实棋子** — RadialGradientPaint 径向渐变模拟黑曜石/珍珠光照质感
- **悬停预览** — 半透明棋子 + 十字准心辅助定位
- **最后落子标记** — 红点指示最近一步
- **获胜动画** — 五连子金色连线脉冲发光
- **悔棋 / 重新开始 / 比分统计**
- **AI 启发式搜索** — 棋型评分 + 攻防加权 + 靠近中心加分

## 快速开始

```bash
# 编译
mvn compile

# 运行测试
mvn test

# 打包可执行 JAR
mvn package

# 运行
java -jar target/gomoku-1.0-SNAPSHOT.jar
```

## AI 算法

启发式棋型评分，对每个候选空位计算：

| 棋型 | 分数 |
|------|------|
| 连五 | 100,000 |
| 活四 | 10,000 |
| 冲四 / 活三 | 1,000 |
| 活二 | 100 |
| 冲三 / 活一 | 10 |

- 攻击分 × 1.1 + 防守分 = 综合得分
- 越靠近天元（中心）加分越多
- 仅搜索已有棋子周围 2 格，剪枝提效

## License

MIT
