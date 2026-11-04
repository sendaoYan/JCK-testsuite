# javax.swing.plaf.synth.SynthGraphicsUtils — DESIGN

## 1. API 功能
`SynthGraphicsUtils` 提供 Synth 文本/图标测量与绘制工具（public 普通类，非抽象，`extends Object`，无接口）。实例方法 `computeStringWidth`/`getMinimumSize`/`getMaximumSize`/`getPreferredSize`/`getMaximumCharHeight`/`paintText`/`drawLine`；静态 `getIconWidth`/`getIconHeight`/`paintIcon`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthGraphicsUtils.java`（class=54、computeStringWidth=187、getMaximumCharHeight=267、getPreferredSize=290、getIconWidth=445、getIconHeight=463）。

## 2. Java 用户如何使用
`new SynthGraphicsUtils()` 后调用：`g.computeStringWidth(ctx, font, fm, "hello")`；`SynthGraphicsUtils.getIconWidth(icon, ctx)` 静态取图标宽。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthGraphicsUtils.java:445`：`getIconWidth` 对 `SynthIcon` 走 `getIconWidth(ctx)`，普通 `Icon` 走 `getIconWidth()`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthStyle.java:744`：`getGraphicsUtils` 默认返回共享实例。

## 4. 测试用例设计（黑盒部分，`SynthGraphicsUtilsTest.testBlackBox`）
1. `new SynthGraphicsUtils()` 构造。
2. `computeStringWidth` 与 `getMaximumCharHeight` 正值。
3. `getPreferredSize`/`getMinimumSize`/`getMaximumSize` 尺寸非负。
4. `paintText`/`drawLine` 于 `BufferedImage` Graphics2D 正常完成。
5. `getIconWidth/getIconHeight`：`ImageIcon(100x50)` 返回 100/50，`null` 返回 0/0。
