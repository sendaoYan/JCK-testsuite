# javax.swing.text.StyledEditorKit.AlignmentAction — DESIGN

## 1. API 功能
`extends StyledTextAction`，设置段落对齐。`AlignmentAction(String nm, int a)`（a 为 StyleConstants.ALIGN_LEFT/CENTER/RIGHT）。`actionPerformed`：默认用构造器对齐值，若事件源是目标编辑器且 action command 可解析为整数则覆盖，然后 `setParagraphAttributes(editor, {Alignment:a}, false)`。

## 2. Java 用户如何使用
`new AlignmentAction("center-justify", StyleConstants.ALIGN_CENTER)` 触发后把选区所在段落的 Alignment 属性设为居中。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`defaultActions` 含 `new AlignmentAction("left-justify", ALIGN_LEFT)`、`center-justify`、`right-justify`。
- `src/java.desktop/share/classes/javax/swing/JTextPane.java`：`setParagraphAttributes` 委托给 StyledDocument（同族段落属性路径）。

## 4. 测试用例设计（黑盒部分，`AlignmentActionTest.testBlackBox`）
1. 名称/启用断言。
2. 构造器默认对齐：选中段落后触发，段落 Alignment == ALIGN_CENTER。
3. command 覆盖：command "2" 使对齐变为 ALIGN_RIGHT。
