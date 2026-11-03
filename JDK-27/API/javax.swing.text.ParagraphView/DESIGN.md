# javax.swing.text.ParagraphView — DESIGN

## 1. API 功能
支持多字体/换行的段落视图（`extends FlowView`，implements `TabExpander`）。核心方法：
- 构造：`ParagraphView(Element)`（读段落属性初始化 justification/lineSpacing/indent）。
- 轴/约束：`getFlowSpan/getFlowStart`（含行内边距调整）、`getFlowAxis()`（X_AXIS）。
- 制表（TabExpander）：`nextTabStop(float,int)`（默认每 72 点一个制表位，非左对齐 +10）。
- 对齐/断行：`getAlignment(axis)`（X=0.5，Y 按首行算）、`getBreakWeight/breakView`（Y 轴 BadBreakWeight）。
- 布局：`createRow()`（内部 `class Row extends BoxView`）、`getLayoutViewCount/getLayoutView`。

## 2. Java 用户如何使用
StyledEditorKit 为 Paragraph 元素创建 `new ParagraphView(elem)`；需 StyledDocument（JTextPane）上下文。用户查询 `getFlowAxis/nextTabStop/getAlignment` 等，布局由 FlowStrategy 驱动。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java:317`：Paragraph 元素 `new ParagraphView(elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java:410`：`nextTabStop` 默认 `(((int)x/72+1)*72)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java:821`：`class Row extends BoxView`。

## 4. 测试用例设计（黑盒部分，`ParagraphViewTest.testBlackBox`）
1. JTextPane 段落元素构造：getElement/offsets、`getAxis==Y_AXIS`、`getFlowAxis==X_AXIS`。
2. `getFlowSpan(0)==Integer.MAX_VALUE`、`getFlowStart(0)==0`。
3. `nextTabStop(0)==72`、`nextTabStop(100)==144`。
4. `getAlignment(X)==0.5`、`getAlignment(Y)==0.5`、非法轴 → IAE。
5. `getBreakWeight` 两轴均 BadBreakWeight；`breakView(Y_AXIS,...,null)==this`。
