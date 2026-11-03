# javax.swing.text.DefaultEditorKit.PasteAction — DESIGN

## 1. API 功能
`DefaultEditorKit` 的公开静态嵌套类（`public static class PasteAction extends TextAction`），把系统剪贴板内容粘贴到文本组件。
- 构造器：`PasteAction()`（无参，`super(pasteAction)`，动作名 `DefaultEditorKit.pasteAction = "paste-from-clipboard"`）。
- 核心操作：`actionPerformed(ActionEvent e)` —— 定位 `JTextComponent` 后调用 `target.paste()`；`paste()` 仅在 `isEditable() && isEnabled()` 时执行。有选区则替换选区，无选区则在光标处插入。

## 2. Java 用户如何使用
先向系统剪贴板写入内容（`Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(...), null)`），再 `new DefaultEditorKit.PasteAction()` 触发 `actionPerformed`，内容被插入到光标/替换选区。非可编辑组件为 no-op。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`（`PasteAction.actionPerformed`）：`JTextComponent target = getTextComponent(e); if (target != null) target.paste();`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`（`paste()`）：`if (isEditable() && isEnabled()) invokeAction("paste", TransferHandler.getPasteAction());`。
- 同文件 `DefaultTransferHandler.importData`：读 `Transferable` 的 `stringFlavor`，`((JTextComponent)comp).replaceSelection(data)`。
- 同文件 `DefaultTransferHandler.canImport`：`!(c.isEditable() && c.isEnabled())` 时拒绝导入。

## 4. 测试用例设计（黑盒部分，`PasteActionTest.testBlackBox`）
1. `new PasteAction()` 是 `Action`，动作名为 `"paste-from-clipboard"`。
2. 写剪贴板 "Pasted" 后，在 JTextArea("Before") 光标 0 处触发 paste：文本变 "PastedBefore"（光标插入）。
3. JTextArea("ABCDEF") 选 1..4，写剪贴板 "X" 后触发 paste：文本变 "AXEF"（替换选区）。
4. 非可编辑组件触发 paste 为 no-op（文本不变）。
