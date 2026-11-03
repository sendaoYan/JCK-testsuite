# javax.swing.text.StyledEditorKit.BoldAction — DESIGN

## 1. API 功能
`extends StyledTextAction`，`BoldAction()` 名称为 "font-bold"。`actionPerformed`：读取 `StyledEditorKit.getInputAttributes()` 当前 Bold 状态并取反，用 `setCharacterAttributes(editor, {Bold:on/off}, false)` 应用到选区（无选区则写入输入属性）。

## 2. Java 用户如何使用
`new BoldAction()` 绑定到工具栏粗体按钮，选中文本后触发切换粗体。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`defaultActions` 含 `new BoldAction()`；`actionPerformed` 用 `StyleConstants.isBold(attr)` 取反。
- `src/java.desktop/share/classes/javax/swing/JTextPane.java`：`setCharacterAttributes` 的 `StyleConstants.Bold` 是同一属性键。

## 4. 测试用例设计（黑盒部分，`BoldActionTest.testBlackBox`）
1. 名称/启用断言。
2. 选中 0..5 触发 → 该字符 run isBold 为 true；再触发 → 切回 false（切换语义）。
