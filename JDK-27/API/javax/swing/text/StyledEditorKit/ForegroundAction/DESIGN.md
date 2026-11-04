# javax.swing.text.StyledEditorKit.ForegroundAction — DESIGN

## 1. API 功能
`extends StyledTextAction`，`ForegroundAction(String nm, Color fg)`。`actionPerformed`：默认用构造器前景色，事件源是目标编辑器时用 `Color.decode(command)` 覆盖（解析失败保留默认），fg 非空时 `setCharacterAttributes(editor, {Foreground:fg}, false)`。

## 2. Java 用户如何使用
`new ForegroundAction("foreground-color", Color.RED)` 应用到选区设置前景色；command 传 "#00FF00" 之类动态改色。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：ForegroundAction 的 `actionPerformed` 调 `Color.decode(s)` 并 `StyleConstants.setForeground`。
- `src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`setForeground/getForeground`（未设置时默认 Color.black）。

## 4. 测试用例设计（黑盒部分，`ForegroundActionTest.testBlackBox`）
1. 名称/启用断言。
2. 解析失败的 command（"not-a-color"）保留默认 RED。
3. 合法 command "#00FF00" 覆盖为绿色。
