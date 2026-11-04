# javax.swing.JMenu 黑盒行为测试设计

## 1. API 功能
JMenu 是带弹出子菜单的菜单项，`extends JMenuItem implements Accessible, MenuElement`。

- 构造器：`JMenu()`、`JMenu(String)`、`JMenu(Action)`、`JMenu(String, boolean)`。
- 子项管理：`add(JMenuItem)`、`add(Component)`、`add(String)`、`add(Action)`、`addSeparator()`、`insert(String,int)`、`insert(JMenuItem,int)`、`insert(Action,int)`、`insertSeparator(int)`、`remove(JMenuItem)`、`remove(int)`、`remove(Component)`、`removeAll()`。
- 查询：`getItem(int)`、`getItemCount()`、`getMenuComponent(int)`、`getMenuComponentCount()`、`getMenuComponents()`、`isMenuComponent(Component)`。
- 行为属性：`getDelay()`/`setDelay(int)`、`isTopLevelMenu()`、`isTearOff()`、`getPopupMenu()`、`isPopupMenuVisible()`/`setPopupMenuVisible(boolean)`、`setAccelerator(KeyStroke)`（抛 Error）。
- 事件：`addMenuListener`/`removeMenuListener`/`getMenuListeners`、`doClick(int)`（设置 MenuSelectionManager 选中路径）。

## 2. Java 用户如何使用
```java
JMenu file = new JMenu("File");
file.add("Open");                     // 返回 JMenuItem("Open")
file.add(new JMenuItem("Save"));
file.addSeparator();
file.insert("New", 0);                // 头部插入
file.getItem(0).getText();            // "New"
file.getItemCount();                  // 4
file.getDelay();                      // 200（JDK-27 默认）
JMenuBar bar = new JMenuBar();
bar.add(file);
file.isTopLevelMenu();                // true
```

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev/test/jdk/javax/swing/**`。

- `JMenuItem/JMenuItemSetAcceleratorTest.java`（JDK test/jdk）：`JMenu menu = new JMenu("File"); ... menu.add(menuItem); bar.add(menu);`。
- `JMenuItem/JActionCommandTest.java`（JDK test/jdk）：`JMenu`/`JMenuItem` 组合 + Robot 点击。
- `JMenu/bug4244796.java`（JDK test/jdk）：`JMenu menu = new JMenu(action);` 构造器直接接 `Action`。
- `JMenuItem/TestRadioAndCheckMenuItemWithIcon.java`（JDK test/jdk）：`JMenu topLevel = new JMenu("Menu"); topLevel.add(button1); topLevel.addSeparator();`。

## 4. 测试用例设计 + 自动化实现
指向 `JMenuTest.java` 的 `testBlackBox()`：
- 构造器：`JMenu()` 文本为空串（非 null）、`JMenu(String)`、`JMenu(String,boolean)`、`getUIClassID()=="MenuUI"`。
- `getDelay()` 默认 200（JDK-27 实测，与 jdk25u javadoc 的 0 不同）、`setDelay` 往返、`setDelay(-1)` 抛 `IllegalArgumentException`。
- 组合链路：`add(String)`→`getItemCount`→`getItem`；`add(JMenuItem)`；`addSeparator`；`insert`/`insertSeparator`；`getMenuComponentCount/Components/Component`；`remove(JMenuItem)`/`remove(int)`/`removeAll`。
- `isMenuComponent`、`getPopupMenu` 恒等。
- `isTopLevelMenu` 依赖 JMenuBar 父容器。
- `setAccelerator`/`isTearOff` 抛 `Error`。
- `doClick(0)` 后 MenuSelectionManager 选中路径为 `[menubar, menu, popup]`（随后 clear）。
- `isPopupMenuVisible` 默认 false + `setPopupMenuVisible(false)` 幂等。
- MenuListener 基线相对 add/remove 往返。
