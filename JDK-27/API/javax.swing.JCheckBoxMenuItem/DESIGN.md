# javax.swing.JCheckBoxMenuItem 黑盒行为测试设计

## 1. API 功能
JCheckBoxMenuItem 是可勾选的菜单项，`extends JMenuItem implements SwingConstants, Accessible`。内部用 `JToggleButton.ToggleButtonModel`。

- 构造器：`JCheckBoxMenuItem()`、`(Icon)`、`(String)`、`(Action)`、`(String,Icon)`、`(String,boolean)`、`(String,Icon,boolean)`。
- 选中状态：`isSelected()`（继承 AbstractButton）、`getState()`/`setState(boolean)`、`getSelectedObjects()`。
- 事件：`doClick(int)` 切换选中并触发 ItemEvent + ActionEvent；`addItemListener`。
- 继承 JMenuItem：`setAccelerator/getAccelerator`、`setMnemonic/getMnemonic`、`getSubElements`、`getComponent`。

## 2. Java 用户如何使用
```java
JCheckBoxMenuItem c = new JCheckBoxMenuItem("Wrap", true);
c.getState();                       // true
c.getSelectedObjects()[0];          // "Wrap"
c.addItemListener(e -> {
    if (e.getStateChange() == ItemEvent.SELECTED) { ... }
});
c.doClick(0);                       // 切换为 false，触发 DESELECTED + ActionEvent
c.setState(true);                   // 重新选中
```

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev/test/jdk/javax/swing/**`。

- `JMenuItem/TestRadioAndCheckMenuItemWithIcon.java`（JDK test/jdk，bug 8348760）：
  ```java
  AbstractButton check1 = new JCheckBoxMenuItem("JCheckBoxMenuItem 1", imageIcon1);
  check1.setSelected(true);
  JCheckBoxMenuItem c3 = new JCheckBoxMenuItem("JCheckBoxMenuItem 3");
  c3.setAccelerator(KeyStroke.getKeyStroke(VK_F5, ALT|CTRL|SHIFT));
  topLevel.add(check1); topLevel.add(check2); topLevel.add(check3);
  ```
- `JMenuItem/8031573/bug8031573.java`、`JMenuItem/RightLeftOrientation.java`：JCheckBoxMenuItem 与 JRadioButtonMenuItem 混排。

## 4. 测试用例设计 + 自动化实现
指向 `JCheckBoxMenuItemTest.java` 的 `testBlackBox()`：
- 构造器：默认未选中/文本空串、`(String,boolean)` 双向、`getUIClassID()=="CheckBoxMenuItemUI"`。
- 往返：`setState(true/false)`→`isSelected/getState`；`getSelectedObjects()` 选中/未选中。
- ItemEvent 链路：`setSelected(true)`→SELECTED、`setSelected(false)`→DESELECTED、重复同值静默。
- `doClick` 组合：切换选中 + ActionEvent + ItemEvent 顺序与来源。
- 禁用：`setEnabled(false)` 后 `doClick` 不切换、不触发 action。
- 继承链路：`getSubElements/getComponent/setAccelerator/setMnemonic`。
