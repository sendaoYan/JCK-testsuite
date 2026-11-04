# javax.swing.CellRendererPane 设计案

## 1. API 功能
`CellRendererPane` 是继承 `java.awt.Container` 的轻量渲染容器，供表格/树/列表绘制单元格渲染器，不参与焦点与事件。方法分组：
- 绘制：`paintComponent(Graphics, Component, Container, ...)` 三个重载、`paint`、`update`。
- 无效化：`invalidate()`（覆盖为无操作，避免触发重绘）。
- 可访问：`getAccessibleContext()`。

## 2. Java 用户如何使用
渲染器通常在 `paintComponent` 中把单元格渲染器画到目标 `Graphics`：
```java
CellRendererPane pane = new CellRendererPane();
pane.paintComponent(g, renderer, table, x, y, w, h, hasFocus);
```
典型链路：`new CellRendererPane` → `paintComponent(g, renderer, parent, rect)` → `paint/update`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicTableUI.java`、`BasicListUI.java`、`BasicTreeUI.java` 均使用 `CellRendererPane.paintComponent` 绘制单元格（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/`）。
- JDK 源码 `javax/swing/JList.java` 等内部持有 `CellRendererPane`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `java.awt.Container`、实现 `Accessible`。
- 行为（黑盒）：`testBlackBox()` 用离屏 `BufferedImage` 覆盖三个 `paintComponent` 重载、`paint/update` 的 smoke 绘制、`invalidate` 无操作、`getAccessibleContext` 非空。
