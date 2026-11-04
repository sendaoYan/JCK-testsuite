# javax.swing.Action 设计案

## 1. API 功能
`Action` 是 Swing 命令抽象，继承 `java.awt.event.ActionListener`。定义一组标准键常量（`NAME`、`SHORT_DESCRIPTION`、`ACCELERATOR_KEY` 等）。方法分组：
- 键常量：`DEFAULT/NAME/SHORT_DESCRIPTION/LONG_DESCRIPTION/SMALL_ICON/ACTION_COMMAND_KEY/ACCELERATOR_KEY/MNEMONIC_KEY/SELECTED_KEY/DISPLAYED_MNEMONIC_INDEX_KEY/LARGE_ICON_KEY`。
- 值存取：`getValue(String)`、`putValue(String, Object)`。
- 状态：`isEnabled()`、`setEnabled(boolean)`。
- 默认方法：`accept(Object)`（默认 true）。
- 监听：`addPropertyChangeListener`、`removePropertyChangeListener`。

## 2. Java 用户如何使用
`Action` 由 `AbstractAction` 实现；用户把 `Action` 绑定到按钮/菜单/按键映射，共享命令状态：
```java
Action a = new AbstractAction("Copy") { public void actionPerformed(ActionEvent e) {} };
a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
button.setAction(a);
```
典型链路：`putValue(NAME/ACCELERATOR_KEY)` → `getValue` → `isEnabled/setEnabled` → `accept` → `actionPerformed`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/AbstractAction.java` 实现该接口全部抽象方法（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/AbstractAction.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 等用 `Action.ACCELERATOR_KEY` 等常量配置按键绑定。
- JDK 源码 `javax/swing/JButton.java`、`JMenuItem.java` 通过 `setAction(Action)` 消费该接口。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 isInterface、父接口 `ActionListener`。
- 行为（黑盒）：`testBlackBox()` 覆盖全部 11 个键常量字符串、匿名实现的 `putValue/getValue`、`isEnabled/setEnabled`、默认方法 `accept` 返回 true、`actionPerformed` 可调用。
