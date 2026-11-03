# javax.swing.text.PasswordView — DESIGN

## 1. API 功能
密码框回显视图（`extends FieldView`）。核心方法：
- 尺寸：继承 FieldView 的 `getPreferredSpan/getMinimumSpan/getMaximumSpan`；X 方向可伸缩（minimum 0、maximum MAX_VALUE）。
- 映射：`modelToView/viewToModel` 按回显字符（echo char）宽度列映射；`getNextVisualPositionFrom`。
- 绘制 `paint` 使用 `drawEchoCharacter` 逐字符绘制遮蔽符；`getResizeWeight` 继承 FieldView。

## 2. Java 用户如何使用
`BasicPasswordFieldUI` 的 ViewFactory 为根元素 `new PasswordView(elem)`；用户从 `JPasswordField("secret").getUI().getRootView(pf).getView(0)` 取得真实实例，查询回显宽度与 caret 列映射。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicPasswordFieldUI.java:92`：`return new PasswordView(elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthPasswordFieldUI.java`：synth LAF 同样复用 PasswordView。

## 4. 测试用例设计（黑盒部分，`PasswordViewTest.testBlackBox`）
1. 根元素构造：`getElement/getStartOffset/getEndOffset` 一致。
2. 真实 JPasswordField 根子视图 instanceof PasswordView。
3. 回显字符宽度下 X 跨度 > 0、Y > 0；`getMinimumSpan(X)==0`、`getMaximumSpan(X)==Integer.MAX_VALUE`。
4. `modelToView` 每位置映射到回显字符列（0 与 3 的 x 递增、caret 宽 1）；`viewToModel` 逆向映射左边缘→0、列→对应位置。
