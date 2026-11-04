# javax.swing.text.DefaultEditorKit.InsertBreakAction — DESIGN

## 1. API 功能
`extends TextAction`，`InsertBreakAction()` 名称为 `insertBreakAction="insert-break"`。`actionPerformed`：目标可编辑且可用时 `replaceSelection("\n")` 插入换行（有选区则先替换）。

## 2. Java 用户如何使用
作为 DefaultEditorKit 内置动作，绑定回车键：`new InsertBreakAction()` → 触发后文本组件在插入符处插入换行。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java`：`map.get(DefaultEditorKit.insertBreakAction)` 取回后 `instanceof DefaultEditorKit.InsertBreakAction` 并包装成 UI 动作。
- `src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`：`defaultActions` 数组含 `new InsertBreakAction()`。

## 4. 测试用例设计（黑盒部分，`InsertBreakActionTest.testBlackBox`）
1. 类型/名称/启用断言。
2. 在插入符处插入 `\n`（"ab" 光标 1 → "a\nb"）。
3. 选区被换行替换（"abcd" 选 1..3 → "a\nd"）。
