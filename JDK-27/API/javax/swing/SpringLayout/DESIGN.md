# javax.swing.SpringLayout 设计案

## 1. API 功能
`SpringLayout` 用 Spring 约束布局组件。方法分组：
- 约束：`putConstraint/getConstraint/getConstraints`。
- 布局：`preferredLayoutSize/minimumLayoutSize/maximumLayoutSize/layoutContainer`。
- 对齐：`getLayoutAlignmentX/getLayoutAlignmentY`。

## 2. Java 用户如何使用
用户用 Spring 约束组件位置：
```java
SpringLayout layout = new SpringLayout();
panel.setLayout(layout);
layout.putConstraint(SpringLayout.WEST, btn, 5, SpringLayout.WEST, panel);
```
典型链路：`new SpringLayout` → `putConstraint` → `getConstraint/getConstraints`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SpringLayout.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SpringLayout.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 `LayoutManager2`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `putConstraint/getConstraint/getConstraints`、三种尺寸、对齐、`addLayoutComponent/removeLayoutComponent/invalidateLayout/layoutContainer`。
