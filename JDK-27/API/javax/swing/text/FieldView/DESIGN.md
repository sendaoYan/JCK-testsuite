# javax.swing.text.FieldView — DESIGN

## 1. API 功能
单行文本字段视图（`extends PlainView`）。核心方法：
- 尺寸：`getPreferredSpan/getMinimumSpan/getMaximumSpan`；`getResizeWeight`（X_AXIS=1 可伸缩、Y_AXIS=0 固定行高）。
- 映射：`modelToView/viewToModel`、`getPreferredSpan(X_AXIS)` 取文本宽度。
- 继承 PlainView 的 `nextTabStop/getTabbedSpan` 与 `paint`。

## 2. Java 用户如何使用
`BasicTextFieldUI` 的 ViewFactory 为根元素 `new FieldView(elem)`；用户可 `new FieldView(rootElement)` 后 setParent 到带 Container 的父视图查询宽度与行高，或从 `JTextField.getUI().getRootView(tc).getView(0)` 取得真实实例。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextFieldUI.java:112`：`return new FieldView(elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/PasswordView.java`：`extends FieldView`，用于密码框回显字符。

## 4. 测试用例设计（黑盒部分，`FieldViewTest.testBlackBox`）
1. 根元素构造：`getElement/getStartOffset/getEndOffset` 一致。
2. `getResizeWeight(X)==1`、`getResizeWeight(Y)==0`。
3. `getPreferredSpan` X >= 0、Y > 0（单行高）；`getMinimumSpan(X)==0`、`getMaximumSpan(X)==Integer.MAX_VALUE`（水平可伸缩）。
4. `modelToView(0)` 左边缘、单列 caret、x 单调；`viewToModel` 左边缘→0、Forward 偏置。
