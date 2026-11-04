# javax.swing.text.DefaultEditorKit.DefaultKeyTypedAction — DESIGN

## 1. API 功能
`extends TextAction`，处理键盘键入字符。`DefaultKeyTypedAction()` 用 `defaultKeyTypedAction="default-typed"` 作为动作名。`actionPerformed(ActionEvent)`：从事件源取 JTextComponent，可编辑且可用时，把事件 action command 的首字符（可打印字符）通过 `replaceSelection` 插入文档；内容为空时不动作。

## 2. Java 用户如何使用
`new DefaultEditorKit.DefaultKeyTypedAction()`，作为 JTextComponent 默认 keymap 的默认动作，在 KEY_TYPED 且无绑定按键时被触发。测试中构造 `ActionEvent(area, ACTION_PERFORMED, command)` 触发。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`：初始化默认 keymap `binding.setDefaultAction(new DefaultEditorKit.DefaultKeyTypedAction())`——每个文本组件的默认按键动作。
- `src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`：`defaultActions` 数组含 `new DefaultKeyTypedAction()`。

## 4. 测试用例设计（黑盒部分，`DefaultKeyTypedActionTest.testBlackBox`）
1. 实例类型（TextAction/Action）、名称 default-typed、默认启用。
2. headless-safe 路径：null 事件、null command、空 command、非文本源四类输入下 `actionPerformed` 均不改文档、不抛异常（避免触发 Toolkit 分支）。
