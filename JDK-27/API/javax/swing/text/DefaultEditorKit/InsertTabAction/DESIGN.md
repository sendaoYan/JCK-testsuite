# javax.swing.text.DefaultEditorKit.InsertTabAction — DESIGN

## 1. API 功能
`extends TextAction`，`InsertTabAction()` 名称为 `insertTabAction="insert-tab"`。`actionPerformed`：目标可编辑且可用时 `replaceSelection("\t")` 插入制表符。

## 2. Java 用户如何使用
作为 DefaultEditorKit 内置动作绑定 Tab 键：触发后在插入符处插入 `\t`（有选区先替换）。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`：`defaultActions` 数组含 `new InsertTabAction()`；`insertTabAction="insert-tab"` 常量。
- `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java` 等通过动作名从 keymap/ActionMap 取回这些内置动作。

## 4. 测试用例设计（黑盒部分，`InsertTabActionTest.testBlackBox`）
1. 类型/名称/启用断言。
2. 空文档插入 `\t`；"ab" 光标 1 → "a\tb"。
3. 选区替换（"ab" 选 0..2 → "\t"）。
