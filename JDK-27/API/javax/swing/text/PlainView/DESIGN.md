# javax.swing.text.PlainView — DESIGN

## 1. API 功能
纯文本单字体视图（`extends View`，implements `TabExpander`）。核心方法：
- 尺寸：`getPreferredSpan/getMinimumSpan/getMaximumSpan`（X 取最长行宽、Y 取 `getElement().getElementCount() * 行高`）。
- 制表：`nextTabStop(float,int)`（8 字符网格）、`getTabbedSpan/getPartialSpan`。
- 映射：`modelToView/viewToModel`（按行矩形 + `Utilities.getTabbedTextWidth/Offset`）、`getNextVisualPositionFrom`。
- 断行：`breakView/getBreakWeight`；`getResizeWeight`（Y 不可伸缩）；`getFontMetrics/updateMetrics/calculateLongestLine`。
- `paint` 使用 `Utilities.drawTabbedText`。

## 2. Java 用户如何使用
通常由 `BasicTextAreaUI` 的 ViewFactory 为根元素创建；`new PlainView(rootElement)`（根元素为行映射）后 `setParent` 到含 Container 的父视图，再查询 `getPreferredSpan`/`modelToView`。传入叶元素（无子行）会使 Y 跨度归零，故测试使用根元素。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextAreaUI.java:158`：不换行时 `new PlainView(elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/WrappedPlainView.java`：`extends PlainView` 实现换行。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/FieldView.java`：`extends PlainView` 作为单行文本字段视图。

## 4. 测试用例设计（黑盒部分，`PlainViewTest.testBlackBox`）
1. 用根元素构造：`getElement/getStartOffset/getEndOffset/getDocument` 正确；`getViewCount==0/getView(0)==null`。
2. `setParent(Host)` 后 Y 跨度 > 0（单行高），且 `getMinimumSpan/getMaximumSpan(Y)==Y 跨度`。
3. `nextTabStop`：首个 > 0，且按网格步进（`nextTabStop(ts)==2*ts`），超出参考位置时 >= 参考。
4. `modelToView(0)` 左边缘、单列 caret；`viewToModel` 左边缘→0、Forward 偏置。
5. 真实 JTextArea 根子视图 instanceof PlainView 且高度为正。
