# javax.swing.text.GlyphView.GlyphPainter — DESIGN

## 1. API 功能
字形绘制抽象策略（public abstract static 内部类，`extends Object`）。核心方法：
- 抽象：`getSpan/getHeight/getAscent/getDescent/paint/modelToView/viewToModel/getBoundedPosition`。
- 具体：`getPainter(GlyphView,int,int)`（默认返回 this）、`getNextVisualPositionFrom(GlyphView,pos,bias,shape,direction,biasRet)`（东西移动、南北跨行、非法方向 IAE）。

## 2. Java 用户如何使用
GlyphView 通过 `getGlyphPainter()` 惰性创建默认绘制器；用户可 `setGlyphPainter(custom)` 替换。抽象类需子类实现 8 个抽象方法；`getPainter` 无状态时返回自身、`getNextVisualPositionFrom` 处理 caret 视觉移动。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/GlyphView.java:1147`：`public abstract static class GlyphPainter`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/GlyphView.java:1228`：`getPainter` 默认 `return this`。

## 4. 测试用例设计（黑盒部分，`GlyphPainterTest.testBlackBox`）
1. 具体 DummyPainter：`getPainter(gv,0,5)==this`。
2. EAST：`0→1`（Forward）、`end-1→-1`、`end-2→end-1`。
3. WEST：`5→4`、`0→-1`、`-1→end-1`。
4. NORTH/SOUTH 单行视图 `pos!= -1` → -1。
5. 非法方向 999 → IllegalArgumentException。
