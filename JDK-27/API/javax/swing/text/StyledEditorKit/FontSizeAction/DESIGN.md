# javax.swing.text.StyledEditorKit.FontSizeAction — DESIGN

## 1. API 功能
`extends StyledTextAction`，`FontSizeAction(String nm, int size)`。`actionPerformed`：默认用构造器 size，事件 command 可解析为整数时覆盖，size!=0 时 `setCharacterAttributes(editor, {FontSize:size}, false)`。

## 2. Java 用户如何使用
`new FontSizeAction("font-size-24", 24)` 应用到选区设置字号；command 可动态改字号。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`defaultActions` 含 font-size-8/10/12/14/16/18/24/36/48 八档。
- `src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`setFontSize/getFontSize`（未设置时默认 12）。

## 4. 测试用例设计（黑盒部分，`FontSizeActionTest.testBlackBox`）
1. 名称/启用断言。
2. 默认字号：选中后触发，getFontSize == 24。
3. command 覆盖：command "30" 使字号变为 30。
