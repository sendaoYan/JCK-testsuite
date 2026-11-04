# javax.swing.SwingUtilities 设计案

## 1. API 功能
`SwingUtilities` 提供 Swing 工具方法。方法分组：
- 几何：`computeIntersection/computeUnion/computeDifference/isRectangleContainingRectangle/convertPoint/convertRectangle`。
- 层级：`getRoot/getLocalBounds/getWindowAncestor`。

## 2. Java 用户如何使用
用户用几何工具计算组件交集/并集：
```java
Rectangle area = SwingUtilities.computeIntersection(0, 0, w, h, clip);
```
典型链路：`computeIntersection/computeUnion` → `convertPoint/getWindowAncestor`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SwingUtilities.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SwingUtilities.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `SwingConstants`。
- 行为（黑盒）：`testBlackBox()` 覆盖几何交集/并集/差集/包含关系、`convertPoint/convertRectangle`、`getRoot/getLocalBounds/getWindowAncestor`（headless 非显示组件返回 null）。
