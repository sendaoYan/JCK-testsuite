# javax.swing.text.StyledEditorKit.StyledTextAction — DESIGN

## 1. API 功能
抽象 `extends TextAction`，`StyledTextAction(String nm)`。为样式动作提供受保护的 `getEditor(ActionEvent)`（仅 JEditorPane）、`getStyledDocument/getStyledEditorKit`（类型不符抛 IllegalArgumentException）、`setCharacterAttributes/setParagraphAttributes`（有选区写文档，无选区写输入属性）。

## 2. Java 用户如何使用
用户不直接实例化（抽象类），通过 BoldAction/FontFamilyAction 等子类应用样式；子类内部用这些 protected 方法读写选区/段落属性。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：所有样式动作（FontFamily/FontSize/Foreground/Alignment/Bold/Italic/Underline）都 extends StyledTextAction 并调用其 `setCharacterAttributes`/`setParagraphAttributes`。
- `src/java.desktop/share/classes/javax/swing/text/TextAction.java`：StyledTextAction 的父类，提供 `getTextComponent`。

## 4. 测试用例设计（黑盒部分，`StyledTextActionTest.testBlackBox`）
1. 用测试内 Probe 子类验证构造器设置名称、默认启用。
2. `getEditor`：JTextPane 源返回该 pane；JTextArea 源返回 null（非 JEditorPane）；null 事件返回 null。
3. `getStyledEditorKit` 返回 StyledEditorKit；`getStyledDocument` 返回 DefaultStyledDocument。
