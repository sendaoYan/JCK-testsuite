# javax.swing.JPopupMenu 黑盒行为测试设计

## 1. API 功能
JPopupMenu 是右键/上下文弹出菜单，`extends JComponent implements Accessible, MenuElement`。

- 构造器：`JPopupMenu()`、`JPopupMenu(String label)`。
- 子项管理：`add(JMenuItem)`、`add(String)`、`add(Action)`、`addSeparator()`、`insert(Action,int)`、`insert(Component,int)`、`remove(int)`。
- 显示：`show(Component,int,int)`、`setVisible(boolean)`、`isVisible()`、`pack()`、`setPopupSize(int,int)`。
- 属性：`getLabel()`/`setLabel(String)`、`isLightWeightPopupEnabled()`/`setLightWeightPopupEnabled(boolean)`、`getDefaultLightWeightPopupEnabled()`、`isBorderPainted()`/`setBorderPainted(boolean)`、`getMargin()`。
- 选择模型：`getSelectionModel()`、`setSelected(Component)`。
- invoker：`getInvoker()`/`setInvoker(Component)`。
- 事件：`addPopupMenuListener`/`getPopupMenuListeners`、`addMenuKeyListener`/`getMenuKeyListeners`。
- MenuElement：`getSubElements()`、`getComponent()`、`getComponentAtIndex(int)`、`getComponentIndex(Component)`。

## 2. Java 用户如何使用
```java
JPopupMenu popup = new JPopupMenu("Context");
popup.add("One");
popup.add(new JMenuItem("Two"));
popup.addSeparator();
component.setComponentPopupMenu(popup);   // 平台右键自动触发
popup.show(invoker, x, y);                // 手动显示
popup.isVisible();                        // true
popup.setVisible(false);
```

## 3. 开源代码实际用法调研
来源：WebSearch（NetBeans/IntelliJ 社区版右键菜单通用模式）+ JDK test/jdk。

- 通用模式（WebSearch）：`new JPopupMenu()` → `add(JMenuItem)` → `component.setComponentPopupMenu(popup)`，`addSeparator()` 分隔。
- `JPopupMenu/bug4966109.java`（JDK test/jdk）：
  ```java
  popup = new JPopupMenu();
  popup.add("One"); popup.add("Two"); popup.add("Three");
  label1.setComponentPopupMenu(popup);
  // ... popup.isVisible() 验证
  ```
- `JPopupMenu/bug4197019.java`、`JPopupMenu/8147521/PopupMenuTest.java`：显示/尺寸行为。

## 4. 测试用例设计 + 自动化实现
指向 `JPopupMenuTest.java` 的 `testBlackBox()`：
- 构造器 + label 往返 + `getUIClassID()=="PopupMenuUI"`。
- 组合链路：`add`/`add(String)`/`addSeparator`/`insert(0)`/`insert(超出)`→`getComponentCount`/`getComponentAtIndex`/`getComponentIndex`（含未知组件 -1）。
- `remove(0)`、`remove(-1)`/`remove(超出)` 抛 `IllegalArgumentException`。
- 轻量弹窗开关 + 静态默认值。
- 选择模型：`setSelected` → `getSelectedIndex`；`clearSelection` → -1。
- `isBorderPainted`/`getMargin` 默认与往返；`setPopupSize` 反映到 preferred size。
- MenuElement：`getComponent()==popup`、`getSubElements` 排除 JSeparator。
- invoker 往返；PopupMenuListener 基线相对往返。
- headful：`show(invoker,x,y)`→`isVisible()`、`setVisible(false)`、`pack()`、`dispose`。
