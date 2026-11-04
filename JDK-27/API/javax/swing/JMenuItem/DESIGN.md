# javax.swing.JMenuItem 黑盒行为测试设计

## 1. API 功能
JMenuItem 是菜单中的一项，`extends AbstractButton implements Accessible, MenuElement`。

- 构造器：`JMenuItem()`、`JMenuItem(Icon)`、`JMenuItem(String)`、`JMenuItem(Action)`、`JMenuItem(String, Icon)`、`JMenuItem(String, int)`。
- 文本/图标：`setText/getText`、`setIcon/getIcon`（继承 AbstractButton）。
- 状态：`setEnabled/isEnabled`、`setArmed/isArmed`、`setAccelerator(KeyStroke)/getAccelerator`、`setMnemonic/getMnemonic`、`setActionCommand/getActionCommand`。
- 事件触发：`doClick(int)`（触发 ActionEvent）、`addActionListener`。
- MenuElement：`getSubElements()`、`getComponent()`、`menuSelectionChanged(boolean)`。
- 菜单特有监听：`addMenuDragMouseListener`/`getMenuDragMouseListeners`、`addMenuKeyListener`/`getMenuKeyListeners`。

## 2. Java 用户如何使用
```java
JMenuItem cut = new JMenuItem("Cut");
cut.setAccelerator(KeyStroke.getKeyStroke('C'));
cut.setMnemonic('F');
cut.setActionCommand("cut");
cut.addActionListener(e -> System.out.println(e.getActionCommand()));
cut.doClick(0);   // 触发 action，source == cut，actionCommand == "cut"
cut.isArmed();    // false（doClick 后复位）
cut.getSubElements().length; // 0
```

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev/test/jdk/javax/swing/**`。

- `JMenuItem/JMenuItemSetAcceleratorTest.java`（JDK test/jdk）：
  ```java
  JMenuItem menuItem = new JMenuItem("Menu Item");
  menuItem.setAccelerator(KeyStroke.getKeyStroke(VK_M, META_DOWN_MASK));
  menuItem.addActionListener(e -> actionLatch.countDown());
  menu.add(menuItem);
  ```
- `JMenuItem/JActionCommandTest.java`（JDK test/jdk）：`JMenuItem` 的快捷键/`ActionCommand` 交互。
- `JMenuItem/TestRadioAndCheckMenuItemWithIcon.java`（JDK test/jdk）：`new JMenuItem("MenuItem1")`、`new JMenuItem("MenuItem2", imageIcon)` 加入 `JMenu`。

## 4. 测试用例设计 + 自动化实现
指向 `JMenuItemTest.java` 的 `testBlackBox()`：
- 构造器：`JMenuItem()` 文本为空串、`JMenuItem(String)`、`JMenuItem(String, Icon)`、`getUIClassID()=="MenuItemUI"`。
- 往返：`setText/setIcon/setActionCommand`、`setEnabled/setArmed/setMnemonic/setAccelerator`。
- 组合链路：`setActionCommand("cut-cmd")` → `addActionListener` → `doClick(0)` → 断言 source==item、actionCommand、id；`setEnabled(false)` → `doClick(0)` 不触发。
- `JMenuItem(Action)` 将 `Action.NAME` 复制为 text。
- `getSubElements().length==0`、`getComponent()==item`。
- MenuKey/MenuDragMouse 监听器基线相对 add/remove 往返（UI 可能已装 drag 监听器）。
