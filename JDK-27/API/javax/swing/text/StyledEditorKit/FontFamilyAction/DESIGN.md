# javax.swing.text.StyledEditorKit.FontFamilyAction — DESIGN

## 1. API 功能
`extends StyledTextAction`，`FontFamilyAction(String nm, String family)`。`actionPerformed`：默认用构造器 family，若事件源是目标编辑器且 action command 非空则覆盖，非空时 `setCharacterAttributes(editor, {FontFamily:family}, false)`。

## 2. Java 用户如何使用
`new FontFamilyAction("font-family-Serif", "Serif")` 应用到选区设置字体族；事件 command 可动态改字体族。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`defaultActions` 含 `new FontFamilyAction("font-family-SansSerif","SansSerif")`、Monospaced、Serif 三种。
- `src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`setFontFamily/getFontFamily`（未设置时默认 "Monospaced"）。

## 4. 测试用例设计（黑盒部分，`FontFamilyActionTest.testBlackBox`）
1. 名称/启用断言。
2. 默认字体族：选中后触发，字符 run 的 getFontFamily == "Serif"。
3. command 覆盖：command "Monospaced" 使字体族变为 Monospaced。
