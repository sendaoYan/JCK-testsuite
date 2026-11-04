# javax.swing.text.StyledEditorKit — DESIGN

## 1. API 功能
`extends DefaultEditorKit`，样式化文本编辑器策略。`StyledEditorKit()` 初始化输入属性与跟踪器。`getInputAttributes()` 返回当前输入 MutableAttributeSet；`getCharacterAttributeRun()` 返回当前字符属性 run Element；`createDefaultDocument()` 返回 DefaultStyledDocument；`getActions()` 用 `TextAction.augmentList(super.getActions(), defaultActions)` 追加样式动作；`install/deinstall` 挂/摘 caret+property 监听；`clone()` 深拷贝输入属性；`getViewFactory()` 返回样式视图工厂。

## 2. Java 用户如何使用
JTextPane 默认 `createDefaultEditorKit() { return new StyledEditorKit(); }`。用户 `new StyledEditorKit()` → `createDefaultDocument()`；或用 `getActions()` 取 BoldAction/FontFamilyAction 等应用到 JEditorPane 选择区。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/JTextPane.java`：`protected EditorKit createDefaultEditorKit() { return new StyledEditorKit(); }`；`setCharacterAttributes` 用 `getInputAttributes()` 存样式。
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`defaultActions` 数组列出 FontFamily/FontSize/Alignment/Bold/Italic/Underline 等样式动作；`getActions()` 调 `TextAction.augmentList`。

## 4. 测试用例设计（黑盒部分，`StyledEditorKitTest.testBlackBox`）
1. content type 继承 text/plain、默认文档 DefaultStyledDocument、视图工厂非空。
2. getActions 含样式动作名（font-bold/font-italic/font-underline/font-family-Serif/font-size-24/center-justify）且继承 insert-content。
3. getInputAttributes 非空；install 前 getCharacterAttributeRun 为 null，install 后非 null，deinstall 后回 null。
4. clone 返回不同 StyledEditorKit 且 inputAttributes 非空。
