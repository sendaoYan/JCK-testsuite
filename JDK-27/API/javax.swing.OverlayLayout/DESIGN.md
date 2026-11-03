# javax.swing.OverlayLayout 设计案

## 1. API 功能
`OverlayLayout` 把多个组件重叠布局在同一区域。方法分组：
- 目标容器：`getTarget`。
- 布局计算：`preferredLayoutSize/minimumLayoutSize/maximumLayoutSize/layoutContainer`。
- 对齐：`getLayoutAlignmentX/getLayoutAlignmentY`。

## 2. Java 用户如何使用
用户把多个组件叠放在一个容器：
```java
OverlayLayout layout = new OverlayLayout(panel);
panel.setLayout(layout);
```
典型链路：`new OverlayLayout(container)` → `layoutContainer` → `preferredLayoutSize`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/OverlayLayout.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/OverlayLayout.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 `LayoutManager2` + `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `getTarget`、`layoutContainer`、三种尺寸、居中对齐、`addLayoutComponent/removeLayoutComponent/invalidateLayout`。
