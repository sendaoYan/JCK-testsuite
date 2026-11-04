# javax.swing.text.TabExpander — DESIGN

## 1. API 功能
制表扩展策略接口（`public interface`）。核心方法：
- `float nextTabStop(float x, int tabOffset)`：给定参考 x 与制表符在文本中的偏移，返回下一个制表位位置（点，>=0）。

## 2. Java 用户如何使用
由 ParagraphView 实现（作为段落级制表坐标系）；LabelView 等 `getTabbedSpan(x, e)` 将制表扩展委托给 `e.nextTabStop`。用户可实现自定义制表位（如固定列宽）。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java:58`：`ParagraphView implements TabExpander`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java:410`：`nextTabStop` 默认每 72 点一个制表位。

## 4. 测试用例设计（黑盒部分，`TabExpanderTest.testBlackBox`）
1. 反射：1 个方法 `nextTabStop(float,int)` 返回 float。
2. 具体实现：`nextTabStop(10,3)==34`、`nextTabStop(0,0)==0`。
