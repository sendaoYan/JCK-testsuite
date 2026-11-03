# javax.swing.text.Highlighter.Highlight — DESIGN

## 1. API 功能
单个高亮区间的只读描述接口。方法分组：
- 位置：`getStartOffset()` / `getEndOffset()`（`getStartOffset() <= getEndOffset()`）。
- 绘制器：`getPainter()`（返回 `Highlighter.HighlightPainter`）。

## 2. Java 用户如何使用
通过 `Highlighter.getHighlights()` 得到 `Highlight[]`，逐个读取 `getStartOffset()/getEndOffset()/getPainter()` 判断高亮覆盖区间与绘制器。实现由 `DefaultHighlighter.HighlightInfo`（内部类）提供。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`HighlightInfo`）：`public int getStartOffset() { return p0.getOffset(); } public int getEndOffset() { return p1.getOffset(); } public Highlighter.HighlightPainter getPainter() { return painter; }` —— 内部类实现。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`getHighlights`）：`Highlighter.Highlight[] h = new Highlighter.Highlight[size]; highlights.copyInto(h);` —— 返回 Highlight 数组。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/Highlighter.java`：`public interface Highlight { int getStartOffset(); int getEndOffset(); HighlightPainter getPainter(); }`。

## 4. 测试用例设计（黑盒部分，`HighlightTest.testBlackBox`）
1. 通过 DefaultHighlighter.addHighlight 得到 Highlight，断言 getStartOffset/getEndOffset/getPainter、start<=end。
2. changeHighlight 后 Highlight 偏移同步更新。
3. 多个高亮时数组长度与元素区分、painter 共享。
