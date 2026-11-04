# javax.swing.ScrollPaneLayout 设计案

## 1. API 功能
`ScrollPaneLayout` 布局 `JScrollPane` 的各部分。方法分组：
- 策略：`setVerticalScrollBarPolicy/getVerticalScrollBarPolicy/setHorizontalScrollBarPolicy/getHorizontalScrollBarPolicy`。
- 组件：`syncWithScrollPane/getViewport/getHorizontalScrollBar/getVerticalScrollBar/getRowHeader/getColumnHeader/getCorner`。
- 布局：`layoutContainer/preferredLayoutSize`。

## 2. Java 用户如何使用
用户用 `JScrollPane` 默认的该布局管理滚动区域：
```java
ScrollPaneLayout layout = new ScrollPaneLayout();
scrollPane.setLayout(layout);
layout.syncWithScrollPane(scrollPane);
```
典型链路：`syncWithScrollPane` → `getViewport/getVerticalScrollBar` → `layoutContainer`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/ScrollPaneLayout.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ScrollPaneLayout.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 `LayoutManager` + `ScrollPaneConstants` + `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖滚动条策略 get/set、`syncWithScrollPane` 后各组件非空、行列头/角落默认 null、`layoutContainer/preferredLayoutSize`。
