# javax.swing.text.DefaultEditorKit.InsertContentAction — DESIGN

## 1. API 功能
`extends TextAction`，`InsertContentAction()` 名称为 `insertContentAction="insert-content"`。`actionPerformed`：取 `e.getActionCommand()` 作为内容，非空时 `replaceSelection(content)` 插入（有选区先替换）；command 为 null 时走错误反馈。

## 2. Java 用户如何使用
`new InsertContentAction()` → `actionPerformed(new ActionEvent(area, ACTION_PERFORMED, "文本"))` 在插入符处插入指定文本。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`：`defaultActions` 数组第一个元素即 `new InsertContentAction()`；`insertContentAction="insert-content"` 常量被绑定到粘贴/插入流程。
- `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java`：通过 DefaultEditorKit 动作名查表。

## 4. 测试用例设计（黑盒部分，`InsertContentActionTest.testBlackBox`）
1. 类型/名称/启用断言。
2. 空文档插入 "AB"→"AB"，光标 2 再插 "CD"→"ABCD"。
3. 选区替换（"abcd" 选 1..3 + "X" → "aXd"）。
