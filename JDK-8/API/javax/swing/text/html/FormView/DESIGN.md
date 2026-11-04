# javax.swing.text.html.FormView — DESIGN

## 1. API 功能
HTML 表单视图（`public class FormView extends ComponentView implements ActionListener`），渲染 `<form>` 与 `<input>` 控件。关键成员：
- 常量：`SUBMIT = "Submit Query"`、`RESET = "Reset"`。
- `createComponent()`：按 input type 生成 JButton/JTextField 等（submit/reset 按钮用常量文本）。
- `getMaximumSpan(int axis)`：与 preferred 对齐。
- `submitData(String)`/`actionPerformed(ActionEvent)`：提交/重置动作。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 FORM/INPUT 创建；控件在 setParent 且父容器存在时物化。单元级测试可直接对 form 元素 `new FormView(elem)`（Metal LaF）查询常量、element 身份与模型层 span 契约，不做真实渲染。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/FormView.java`：`createComponent` 中 `"submit"` 构造 `new JButton(SUBMIT)`、`"reset"` 构造 `new JButton(RESET)`。
- `~/git/jdk8u/jdk/test/javax/swing/text/html/7189299/bug7189299.java`：表单视图回归。

## 4. 测试用例设计（黑盒，`FormViewTest`：testShape + testModelLayer，24 断言）
1. testShape：superclass ComponentView、implements ActionListener、createComponent/getMaximumSpan 方法存在。
2. 模型层（Metal LaF + 固定 HTML）：常量 SUBMIT=="Submit Query"、RESET=="Reset"；form 元素身份、offset 往返；未 parenting 时 getViewCount==0；span 契约（>=0、max>=min）。
3. 负例：非法 axis span 查询 → IAE（精确异常）。
