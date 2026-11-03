# javax.swing.DefaultButtonModel 设计案

## 1. API 功能
`DefaultButtonModel` 是 `ButtonModel` 的默认实现，维护按钮的 armed/selected/enabled/pressed/rollover 状态。方法分组：
- 构造：`DefaultButtonModel()`。
- 状态位：`setArmed/isArmed`、`setSelected/isSelected`、`setEnabled/isEnabled`、`setPressed/isPressed`、`setRollover/isRollover`。
- 属性：`setActionCommand/getActionCommand`、`setMnemonic/getMnemonic`、`setGroup/getGroup`。
- 监听：`addActionListener`、`addItemListener`、`addChangeListener` 及各 `get*Listeners`、`getListeners(Class)`、`getSelectedObjects`。

## 2. Java 用户如何使用
用户把 `DefaultButtonModel` 装到 `AbstractButton` 上驱动点击/选择行为：
```java
AbstractButton b = new JButton();
b.setModel(new DefaultButtonModel());
b.getModel().setActionCommand("ok");
```
典型链路：`new DefaultButtonModel` → `setActionCommand` → `setArmed/setPressed` → 触发 `actionPerformed` → `setSelected` → 触发 `itemStateChanged`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JButton.java`、`JToggleButton.java` 等默认使用 `DefaultButtonModel`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/`）。
- JDK 源码 `javax/swing/ButtonGroup.java` 通过 `ButtonModel.setGroup/getGroup` 管理互斥选择。
- JDK 测试 `test/jdk/javax/swing/AbstractButton/` 覆盖按钮模型状态机。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `ButtonModel`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖默认状态位、各 setter 往返、`setActionCommand/setMnemonic`、`setEnabled(false)` 清除 armed/pressed、armed+press/release 触发一次 action、`setSelected` 触发 item 事件、监听计数。
