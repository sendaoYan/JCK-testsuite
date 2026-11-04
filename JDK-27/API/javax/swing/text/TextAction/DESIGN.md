# javax.swing.text.TextAction — DESIGN

## 1. API 功能
抽象 `extends AbstractAction`，供文本组件动作共享定位目标组件。`TextAction(String name)` 设置 Action.NAME；`getTextComponent(ActionEvent)` 优先取事件源为 JTextComponent，否则取当前聚焦文本组件；`getFocusedComponent()`；静态 `augmentList(Action[] list1, Action[] list2)` 合并动作列表（同名 list2 覆盖 list1）。

## 2. Java 用户如何使用
子类 `new DefaultEditorKit.InsertTabAction()` 等，继承 `getTextComponent(e)` 自动找到目标 JTextComponent；`TextAction.augmentList(super.getActions(), defaults)` 组合动作集。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`getActions()` 调 `TextAction.augmentList(super.getActions(), defaultActions)`。
- `src/java.desktop/share/classes/javax/swing/JTextField.java` 与 `JFormattedTextField.java`：`return TextAction.augmentList(super.getActions(), defaultActions);`。
- `src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：同样用 augmentList 扩展动作集。

## 4. 测试用例设计（黑盒部分，`TextActionTest.testBlackBox`）
1. 测试内 Probe 子类验证构造器设置名称、getTextComponent 从事件源取 JTextArea、非文本源/null 返回 null。
2. augmentList：同名 list2 覆盖、去重后数量、list1/list2 独有的保留、空列表边界。
