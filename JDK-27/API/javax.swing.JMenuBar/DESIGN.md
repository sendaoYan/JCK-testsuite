# javax.swing.JMenuBar 黑盒行为测试设计

## 1. API 功能
JMenuBar 是菜单栏容器，`extends JComponent implements Accessible, MenuElement`。

- 构造器：`JMenuBar()`（默认创建 `DefaultSingleSelectionModel`，禁用焦点遍历键）。
- 菜单管理：`add(JMenu)`、`getMenu(int)`、`getMenuCount()`、`getComponentAtIndex(int)`、`getComponentIndex(Component)`。
- 帮助菜单（未实现，抛 Error）：`setHelpMenu(JMenu)`、`getHelpMenu()`。
- 选择模型：`getSelectionModel()`、`setSelectionModel(SingleSelectionModel)`、`setSelected(Component)`、`isSelected()`。
- 外观属性：`isBorderPainted()`/`setBorderPainted(boolean)`、`getMargin()`/`setMargin(Insets)`、`getUI()`/`setUI()`/`updateUI()`/`getUIClassID()`。
- MenuElement：`getSubElements()`、`getComponent()`、`menuSelectionChanged(boolean)`。

## 2. Java 用户如何使用
典型组合链路（窗口菜单栏）：
```java
JMenuBar bar = new JMenuBar();
JMenu file = new JMenu("File");
bar.add(file);                 // 返回 file
bar.getMenu(0);                // == file
bar.getMenuCount();            // 1
bar.setSelected(file);         // 选中第一个菜单
bar.isSelected();              // true
frame.setJMenuBar(bar);        // 挂到窗口
```
边界用法：`getMenu(-1)`/`getMenu(超出)` 返回 `null`；`getComponentIndex(陌生组件)` 返回 `-1`；`getMargin()` 默认零 Insets，`setMargin(null)` 复位；`setHelpMenu`/`getHelpMenu` 抛 `Error`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev/test/jdk/javax/swing/**`（JDK 官方回归测试）。

- `JMenuItem/JMenuItemSetAcceleratorTest.java`（JDK test/jdk）：
  ```java
  JMenuBar bar = new JMenuBar();
  JMenu menu = new JMenu("File");
  JMenuItem menuItem = new JMenuItem("Menu Item");
  menuItem.setAccelerator(KeyStroke.getKeyStroke(VK_M, META_DOWN_MASK));
  menu.add(menuItem); bar.add(menu);
  frame.setJMenuBar(bar);
  ```
- `JMenuItem/JActionCommandTest.java`（JDK test/jdk）：`JMenuBar`/`JMenu`/`JMenuItem` 组合，配合 `Robot` 点击验证菜单。
- `JMenuItem/TestRadioAndCheckMenuItemWithIcon.java`（JDK test/jdk，bug 8348760）：`JMenuBar menuBar = new JMenuBar();` 后 `frame.setJMenuBar(menuBar)`。
- `JFrame/Serialization/JFrameMenuSerializationTest.java`：菜单栏随窗口序列化。

## 4. 测试用例设计 + 自动化实现
指向 `JMenuBarTest.java` 的 `testBlackBox()`：
- 空态：`getMenuCount()==0`、`getMenu(0/-1)==null`、`getComponentAtIndex(0)==null`、`getComponent()==bar`。
- 组合链路：`add` → `getMenuCount` → `getMenu` → `getComponentAtIndex` → `getComponentIndex`（含未知组件 `-1`）→ 二次 add → `getMenu(2)==null`。
- `getSubElements()` 只返回 MenuElement 子项（JMenu）。
- `setHelpMenu/getHelpMenu` 抛 `Error`。
- 选择模型：`setSelected(file)`→`isSelected()`，`setSelected(陌生组件)` 清除选择。
- `setBorderPainted` 往返、`setMargin` 往返（默认零 Insets、null 复位）。
- `remove` 后 `getMenuCount()` 归零。
