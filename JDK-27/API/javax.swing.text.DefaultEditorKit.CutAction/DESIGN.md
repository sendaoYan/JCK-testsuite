# javax.swing.text.DefaultEditorKit.CutAction — DESIGN

## 1. API 功能
`DefaultEditorKit` 的公开静态嵌套类（`public static class CutAction extends TextAction`），把选中文本剪切到系统剪贴板（复制并删除选区）。
- 构造器：`CutAction()`（无参，`super(cutAction)`，动作名 `DefaultEditorKit.cutAction = "cut-to-clipboard"`）。
- 核心操作：`actionPerformed(ActionEvent e)` —— 定位 `JTextComponent` 后调用 `target.cut()`；`cut()` 仅在 `isEditable() && isEnabled()` 时执行。

## 2. Java 用户如何使用
`new DefaultEditorKit.CutAction()` → 在可编辑文本组件上设置选区后触发 `actionPerformed`；内部 `JTextComponent.cut()` 走 `TransferHandler` MOVE 动作：选区写入剪贴板并从文档删除。非可编辑组件上为 no-op。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`（`CutAction.actionPerformed`）：`JTextComponent target = getTextComponent(e); if (target != null) target.cut();`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`（`cut()`）：`if (isEditable() && isEnabled()) invokeAction("cut", TransferHandler.getCutAction());`。
- 同文件 `DefaultTransferHandler.exportToClipboard`：`action == TransferHandler.MOVE` 时 `clipboard.setContents(...)` 后 `doc.remove(p0, p1-p0)` 删除选区。

## 4. 测试用例设计（黑盒部分，`CutActionTest.testBlackBox`）
1. `new CutAction()` 是 `Action`，动作名为 `"cut-to-clipboard"`。
2. JTextArea("CutThis") 选 0..3（"Cut"）后触发 cut：源文本变 "This"（选区被删）。
3. 新建 JTextArea 执行 paste，断言得到 "Cut"（cut→paste 剪贴板往返）。
4. 非可编辑组件触发 cut 为 no-op（文本不变）。
