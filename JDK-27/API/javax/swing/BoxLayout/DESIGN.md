# javax.swing.BoxLayout 设计案

## 1. API 功能
`BoxLayout` 是沿单轴（水平/垂直）排列组件的 `LayoutManager2`。方法分组：
- 常量：`X_AXIS/Y_AXIS/LINE_AXIS/PAGE_AXIS`。
- 构造：`BoxLayout(Container, int axis)`。
- 查询：`getTarget()`、`getAxis()`。
- 布局：`layoutContainer`、`preferredLayoutSize`、`minimumLayoutSize`、`maximumLayoutSize`、`invalidateLayout`、`addLayoutComponent/removeLayoutComponent`、`getLayoutAlignmentX/Y`。

## 2. Java 用户如何使用
用户把 `BoxLayout` 设到容器上，沿一轴排布子组件：
```java
JPanel p = new JPanel();
p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
p.add(new JButton("One"));
p.add(new JButton("Two"));
```
典型链路：`new BoxLayout(container, axis)` → `container.setLayout` → `add(children)` → `layoutContainer`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/Box.java` 内部使用 `BoxLayout`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Box.java`）。
- JDK 源码 `javax/swing/JToolBar.java` 使用 `BoxLayout` 布局工具栏。
- JDK 测试 `test/jdk/javax/swing/BoxLayout/` 覆盖轴与布局行为。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `LayoutManager2`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖四个轴常量、`getTarget/getAxis`、垂直布局下子组件共享 x 坐标与堆叠顺序、`preferredLayoutSize`、`invalidateLayout/removeLayoutComponent`。
