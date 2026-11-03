# javax.swing.plaf.synth.SynthPainter — DESIGN

## 1. API 功能
`SynthPainter` 是 Synth 区域绘制抽象基类（public abstract，`extends Object`，无接口）。为每个区域提供成对的 `paintXxxBackground/Border`（默认空实现），如 `paintButtonBackground/Border`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthPainter.java`（class=62、paintButtonBackground=140、paintButtonBorder=156）。

## 2. Java 用户如何使用
匿名子类覆写特定区域方法：`new SynthPainter() {}`；由 `SynthStyle.getPainter(ctx)` 返回后在 `update`/`paint` 时回调 `paintButtonBackground(ctx, g, x, y, w, h)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthStyle.java:882`：`public SynthPainter getPainter(SynthContext context)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthPainter.java:140`：`paintButtonBackground` 空实现（默认 no-op）。

## 4. 测试用例设计（黑盒部分，`SynthPainterTest.testBlackBox`）
1. `new SynthPainter() {}` 可实例化。
2. 以 `JLabel` + 最小 `SynthStyle` 构造 `SynthContext`，用 `BufferedImage` 的 `Graphics2D` 依次调用 `paintButtonBackground/Border`、`paintLabelBackground/Border`、`paintPanelBackground`、`paintTextFieldBackground`、`paintTreeCellFocus` 均正常完成（默认 no-op 不抛异常）。
