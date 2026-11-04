# javax.swing.JScrollPane 黑盒行为测试设计

## 1. API 功能
JScrollPane 是带滚动条的容器，继承 JComponent，实现 ScrollPaneConstants 与 Accessible。

- 构造器：`JScrollPane()`、`JScrollPane(Component view)`、`JScrollPane(int vsbPolicy, int hsbPolicy)`、`JScrollPane(Component, int, int)`；默认策略 AS_NEEDED。
- 滚动条策略：`getVerticalScrollBarPolicy/setVerticalScrollBarPolicy`、`getHorizontalScrollBarPolicy/setHorizontalScrollBarPolicy`（非法值抛 IAE）。
- 视口：`getViewport/setViewport`、`setViewportView(Component)`。
- 头：`getRowHeader/setRowHeader/setRowHeaderView`、`getColumnHeader/setColumnHeader/setColumnHeaderView`。
- 角落：`getCorner(String)/setCorner(String, Component)`（UPPER/LOWER × LEFT/RIGHT/LEADING/TRAILING，非法 key 抛 IAE）。
- 视口边框：`getViewportBorder/setViewportBorder`。
- 滚动条：`getVerticalScrollBar/setVerticalScrollBar`、`getHorizontalScrollBar/setHorizontalScrollBar`、`createVerticalScrollBar/createHorizontalScrollBar`。
- 滚轮：`isWheelScrollingEnabled/setWheelScrollingEnabled`。
- UI：`getUIClassID()` 返回 "ScrollPaneUI"。

## 2. Java 用户如何使用
- `JScrollPane sp = new JScrollPane(bigTable);` 包裹大组件。
- `sp.setColumnHeaderView(table.getTableHeader());` 固定列头。
- `sp.setRowHeaderView(rowNumbers);` 固定行头。
- `sp.setCorner(JScrollPane.UPPER_RIGHT_CORNER, button);` 放角落控件。
- `sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);` 强制显示。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/javax/swing/JScrollPane/6612531/bug6612531.java` 直接对 JScrollPane 做布局计算。
- `test/jdk/java/awt/TrayIcon/DisposeInActionEventTest.java` 用 JScrollPane 包裹 JTextArea。
- `src/java.desktop/share/classes/javax/swing/JScrollPane.java` 构造时自动创建 viewport + 两根 JScrollBar。
以上来源为 `~/git/jdk25u-dev/test/jdk` 与 JDK 源码。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JScrollPaneTest.testBlackBox()`）：
1. 默认构造：AS_NEEDED 策略 / 非 null 视口与滚动条 / 滚轮启用 / getUIClassID。
2. view 构造器把组件装进 viewport。
3. 滚动条策略往返 + 非法值 IAE。
4. setViewportView/getViewport().getView() 往返。
5. setColumnHeaderView/setRowHeaderView 创建头视口并装 view。
6. setCorner/getCorner 往返 + 非法 key IAE + 未知 key 返回 null。
7. setViewportBorder 往返、setViewport 往返、setVerticalScrollBar/setHorizontalScrollBar 往返。
8. setWheelScrollingEnabled 往返。
9. 嵌套滚动：一个 JScrollPane 作为另一个 JScrollPane 的 view。
