# javax.swing.text.DefaultEditorKit.CopyAction — DESIGN

## 1. API 功能
`DefaultEditorKit` 的公开静态嵌套类（`public static class CopyAction extends TextAction`），把当前选中文本复制到系统剪贴板。
- 构造器：`CopyAction()`（无参，`super(copyAction)`，动作名 `DefaultEditorKit.copyAction = "copy-to-clipboard"`）。
- 核心操作：`actionPerformed(ActionEvent e)` —— 定位 `JTextComponent` 后调用 `target.copy()`。

## 2. Java 用户如何使用
`new DefaultEditorKit.CopyAction()` → 在文本组件上设置选区后，以组件为 source 构造 `ActionEvent` 触发 `actionPerformed`；内部 `JTextComponent.copy()` 把选区文本写入系统剪贴板（`TransferHandler` 的 COPY 动作），源文本与选区保持不变。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`（`CopyAction.actionPerformed`）：`JTextComponent target = getTextComponent(e); if (target != null) target.copy();`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`（`copy()`）：`invokeAction("copy", TransferHandler.getCopyAction())`。
- 同文件 `DefaultTransferHandler.exportToClipboard`：读 `getSelectionStart()/getSelectionEnd()`，`doc.getText(p0, p1-p0)` 生成 `StringSelection` 后 `clipboard.setContents(...)`（COPY 动作不移除）。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultEditorKit/bug4265242.java`：`getActions()` 遍历按 `getValue(Action.NAME)` 匹配后 `actionPerformed(new ActionEvent(...))`。

## 4. 测试用例设计（黑盒部分，`CopyActionTest.testBlackBox`）
1. `new CopyAction()` 是 `Action`，动作名为 `"copy-to-clipboard"`。
2. JTextArea("CopyThis") 选 0..4（"Copy"）后触发 copy：源文本仍为 "CopyThis"、选区仍为 0..4。
3. 新建 JTextArea 执行 paste，断言得到 "Copy"（copy→paste 剪贴板往返，不断言剪贴板对象身份）。
