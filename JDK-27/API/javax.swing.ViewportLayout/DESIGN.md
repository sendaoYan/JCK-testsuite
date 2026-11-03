# javax.swing.ViewportLayout 设计案

## 1. API 功能
`ViewportLayout` 布局 `JViewport`。方法分组：
- 布局：`layoutContainer/preferredLayoutSize`。
- 组件管理：`addLayoutComponent/removeLayoutComponent`。

## 2. Java 用户如何使用
`JViewport` 默认使用该布局：
```java
ViewportLayout layout = new ViewportLayout();
viewport.setLayout(layout);
layout.layoutContainer(viewport);
```
典型链路：`layoutContainer` → `preferredLayoutSize`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/ViewportLayout.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ViewportLayout.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 `LayoutManager` + `Serializable`。
- 行为（黑盒）：`testBlackBox()` 用 `JViewport`（避免对非 JViewport 容器的 `ClassCastException`）覆盖 `layoutContainer/preferredLayoutSize` 与组件 add/remove。
