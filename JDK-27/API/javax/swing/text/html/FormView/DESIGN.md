# javax.swing.text.html.FormView — DESIGN

## 1. API 功能
HTML 表单视图（`public class FormView extends ComponentView implements ActionListener`），渲染 HTML `<form>` 与 `<input>` 提交/重置控件。关键成员：
- 常量：`SUBMIT = "Submit Query"`、`RESET = "Reset"`。
- `createComponent()`：按 input type 生成 JButton/JTextField/JPasswordField 等。
- `getComponent()`：返回已创建的控件（继承 ComponentView，依赖 parent/container 才非 null）。
- `submitData(String data)` / `actionPerformed(ActionEvent)`：提交/重置动作。
- `getMaximumSpan(int axis)`：与 preferred span 对齐。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 FORM/INPUT 标签创建。控件在 `setParent` 且父容器存在时才被物化，因此黑盒测试需先显示窗口并 `root.setSize(600,400)` 物化 FlowView 行，再从视图树取 live FormView，此时 `getComponent()` 才返回非 null 的控件。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FormView.java`：`createComponent` 中 `if ("submit".equals(type))` 构造 `new JButton(SUBMIT)`，`"reset"` 构造 `new JButton(RESET)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/TestFormViewNPE.java`：表单视图 NPE 回归。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/bug4210307.java`：表单相关回归。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/7189299/bug7189299.java`：表单视图回归。

## 4. 测试用例设计（黑盒部分，`FormViewTest.testBlackBox`）
1. 常量 `SUBMIT.equals("Submit Query")`、`RESET.equals("Reset")`。
2. 从视图树取 live FormView，`getComponent()` 非 null 且为 JComponent。
3. `getMaximumSpan` 与 preferred span 关系、span >= 0。
4. `submitData(...)` 无异常（无真实提交上下文）；`setParent` 可调用。
