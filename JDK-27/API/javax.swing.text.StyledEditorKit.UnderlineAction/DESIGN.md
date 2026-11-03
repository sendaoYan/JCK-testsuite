# javax.swing.text.StyledEditorKit.UnderlineAction — DESIGN

## 1. API 功能
`extends StyledTextAction`，`UnderlineAction()` 名称为 "font-underline"。`actionPerformed`：取 `StyledEditorKit.getInputAttributes()` 当前 Underline 状态并取反，`setCharacterAttributes(editor, {Underline:on/off}, false)`。

## 2. Java 用户如何使用
`new UnderlineAction()` 绑定到下划线按钮，选中文本后触发切换下划线。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`defaultActions` 含 `new UnderlineAction()`；`actionPerformed` 用 `StyleConstants.isUnderline(attr)` 取反。
- `src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`setUnderline/isUnderline`（未设置时默认 false）。

## 4. 测试用例设计（黑盒部分，`UnderlineActionTest.testBlackBox`）
1. 名称/启用断言。
2. 选中 0..5 触发 → isUnderline true；再触发 → false（切换语义）。
