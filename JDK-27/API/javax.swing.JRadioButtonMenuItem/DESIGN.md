# javax.swing.JRadioButtonMenuItem 黑盒行为测试设计

## 1. API 功能
JRadioButtonMenuItem 是单选菜单项，`extends JMenuItem implements Accessible`。内部用 `JToggleButton.ToggleButtonModel`，配合 `ButtonGroup` 实现互斥。

- 构造器：`JRadioButtonMenuItem()`、`(Icon)`、`(String)`、`(Action)`、`(String,Icon)`、`(String,boolean)`、`(Icon,boolean)`、`(String,Icon,boolean)`。
- 选中状态：`isSelected()`/`setSelected(boolean)`、`getSelectedObjects()`（继承 AbstractButton）。
- 事件：`doClick(int)` 切换并触发 ItemEvent + ActionEvent；`addItemListener`。
- 互斥：配合 `ButtonGroup.add(...)`、`ButtonGroup.getSelection()`（返回 ButtonModel）、`getButtonCount()`。
- 继承 JMenuItem：`setAccelerator/getAccelerator`、`setMnemonic/getMnemonic`、`getSubElements`、`getComponent`。

## 2. Java 用户如何使用
```java
ButtonGroup group = new ButtonGroup();
JRadioButtonMenuItem r1 = new JRadioButtonMenuItem("Red");
JRadioButtonMenuItem r2 = new JRadioButtonMenuItem("Blue");
group.add(r1); group.add(r2);
r1.setSelected(true);
r2.setSelected(true);                 // r1 自动取消（互斥）
group.getSelection() == r2.getModel();// true
```

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev/test/jdk/javax/swing/**`。

- `JMenuItem/TestRadioAndCheckMenuItemWithIcon.java`（JDK test/jdk，bug 8348760）：
  ```java
  JRadioButtonMenuItem m1 = new JRadioButtonMenuItem("JRadioButtonMenuItem 1", imageIcon1);
  AbstractButton button2 = new JRadioButtonMenuItem("JRadioButtonMenuItem 2", imageIcon2);
  ButtonGroup buttonGroup = new ButtonGroup();
  buttonGroup.add(button1); buttonGroup.add(button2); buttonGroup.add(button3);
  ```
- `JMenuItem/8158566/CloseOnMouseClickPropertyTest.java`、`JMenuItem/RightLeftOrientation.java`：JRadioButtonMenuItem 行为验证。

## 4. 测试用例设计 + 自动化实现
指向 `JRadioButtonMenuItemTest.java` 的 `testBlackBox()`：
- 构造器：默认未选中/文本空串、`(String,boolean)` 双向、`getUIClassID()=="RadioButtonMenuItemUI"`。
- 选中往返 + `getSelectedObjects()` 文本内容。
- ItemEvent 链路：`setSelected(true)`→SELECTED、`setSelected(false)`→DESELECTED、重复同值静默。
- `doClick` 组合：切换 + ActionEvent + ItemEvent。
- ButtonGroup 互斥：`add`→`getButtonCount`、选择 r1、切到 r2 自动取消 r1、`getSelection()==model`。
- 禁用：`setEnabled(false)` 后 `doClick` 不切换。
- 继承链路：`getSubElements/getComponent/setAccelerator/setMnemonic`。
