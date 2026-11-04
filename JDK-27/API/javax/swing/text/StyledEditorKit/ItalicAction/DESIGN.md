# javax.swing.text.StyledEditorKit.ItalicAction — DESIGN

## 1. API 功能
`extends StyledTextAction`，`ItalicAction()` 名称为 "font-italic"。`actionPerformed`：取 `StyledEditorKit.getInputAttributes()` 当前 Italic 状态并取反，`setCharacterAttributes(editor, {Italic:on/off}, false)`。

## 2. Java 用户如何使用
`new ItalicAction()` 绑定到斜体按钮，选中文本后触发切换斜体。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`defaultActions` 含 `new ItalicAction()`；`actionPerformed` 用 `StyleConstants.isItalic(attr)` 取反。
- `src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`setItalic/isItalic`（未设置时默认 false）。

## 4. 测试用例设计（黑盒部分，`ItalicActionTest.testBlackBox`）
1. 名称/启用断言。
2. 选中 0..5 触发 → isItalic true；再触发 → false（切换语义）。
