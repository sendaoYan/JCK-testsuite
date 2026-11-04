# javax.swing.text.TabableView — DESIGN

## 1. API 功能
制表相关视图接口（`public interface`）。核心方法：
- `float getTabbedSpan(float x, TabExpander e)`：给定起始 x 与制表扩展器返回含制表位调整后的期望跨度。
- `float getPartialSpan(int p0, int p1)`：返回不含制表符的 `[p0,p1)` 文本跨度。

## 2. Java 用户如何使用
由 GlyphView/LabelView/ParagraphView 实现；FlowStrategy 在 `layoutRow` 中对 `TabableView` 子视图调用 `getTabbedSpan(x, te)` 以确定制表扩展后的行宽。用户实现时保证 `getTabbedSpan` 与后续 `getPreferredSpan` 一致。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/GlyphView.java`：`GlyphView implements TabableView`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/FlowView.java:470`：`chunkSpan = ((TabableView)v).getTabbedSpan(x, te)`。

## 4. 测试用例设计（黑盒部分，`TabableViewTest.testBlackBox`）
1. 反射：2 个方法；`getTabbedSpan(float,TabExpander)` 与 `getPartialSpan(int,int)` 均返回 float。
2. 具体实现：`getTabbedSpan(5,null)==105`、`getPartialSpan(2,9)==7`。
