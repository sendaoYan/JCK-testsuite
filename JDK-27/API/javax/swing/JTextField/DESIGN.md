# javax.swing.JTextField 黑盒行为测试设计

## 1. API 功能
JTextField 是单行文本输入控件，继承 JTextComponent，实现 SwingConstants。

- 构造器：`JTextField()`、`JTextField(String)`、`JTextField(int columns)`、`JTextField(String, int)`、`JTextField(Document, String, int)`（columns<0 抛 IAE）。
- 文本：`setText/getText`、`getText(int offs, int len)`。
- 文档：`getDocument/setDocument`，默认 PlainDocument。
- 列宽：`getColumns/setColumns`。
- 可编辑：`isEditable/setEditable`。
- 光标：`getCaretPosition/setCaretPosition`。
- 对齐：`getHorizontalAlignment/setHorizontalAlignment`（LEFT/CENTER/RIGHT/LEADING/TRAILING，非法值抛 IAE，默认 LEADING）。
- Action：`setActionCommand`、`addActionListener/removeActionListener/getActionListeners`、`postActionEvent`。
- 滚动可见性：`getHorizontalVisibility()`（BoundedRangeModel）。

## 2. Java 用户如何使用
- `JTextField f = new JTextField("default", 20);` —— 初始化文本与列宽。
- `f.getText()` / `f.setText(...)`；`f.setEditable(false)` 只读。
- `f.setHorizontalAlignment(SwingConstants.RIGHT)` 右对齐数字。
- `f.setCaretPosition(0)` 归位光标。
- `f.addActionListener(e -> ...)` + 回车触发 action；`postActionEvent()` 程序化触发。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/java/awt/Component/PaintGlitchTest/PaintGlitchTest.java` 用 JTextField 作为表单控件。
- `test/jdk/java/awt/Multiscreen/IMCandidateWindowTest.java` 用 JTextField 承载输入法候选。
- `test/jdk/java/awt/Component/DimensionEncapsulation/*` 遍历 JTextField 验证尺寸封装。
真实业务（登录框、搜索框）通用模式：`new JTextField(20)` + `setActionCommand` + `addActionListener`。以上来源为 `~/git/jdk25u-dev/test/jdk`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JTextFieldTest.testBlackBox()`）：
1. 默认值：空文本 / columns 0 / 可编辑 / PlainDocument / LEADING 对齐。
2. 各构造器文本与列宽语义（含自定义 Document 引用保留）。
3. setText/getText + 光标链（setCaretPosition 往返、空文本复位）。
4. columns 往返（含 0）、editable 往返、setDocument 往返。
5. horizontalAlignment 四种常量往返 + 非法值 IAE。
6. setActionCommand + addActionListener + postActionEvent 触发并校验 actionCommand。
7. getHorizontalVisibility 非 null 且范围合法。
