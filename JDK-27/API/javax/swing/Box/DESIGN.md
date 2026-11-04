# javax.swing.Box 设计案

## 1. API 功能
`Box` 是使用 `BoxLayout` 布局的轻量容器，提供创建刚性/弹性空间的工厂方法。方法分组：
- 构造：`Box(int axis)`。
- 工厂：`createHorizontalBox()`、`createVerticalBox()`。
- 空间：`createRigidArea(Dimension)`、`createHorizontalStrut(int)`、`createVerticalStrut(int)`。
- 胶水：`createGlue()`、`createHorizontalGlue()`、`createVerticalGlue()`。

## 2. Java 用户如何使用
用户用 `Box` + 胶水/支柱在工具栏/对话框里弹性排布控件：
```java
Box bar = Box.createHorizontalBox();
bar.add(button);
bar.add(Box.createHorizontalStrut(10));
bar.add(Box.createHorizontalGlue());
```
典型链路：`createHorizontalBox` → `add(component)` → `add(createHorizontalStrut)` → `add(createHorizontalGlue)`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JToolBar.java` 内部使用 `Box`/`BoxLayout` 布局（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JToolBar.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicOptionPaneUI.java` 用 `Box` 排布按钮。
- JDK 测试 `test/jdk/javax/swing/Box/` 覆盖各 `create*` 工厂方法。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `JComponent`、实现 `Accessible`。
- 行为（黑盒）：`testBlackBox()` 覆盖水平/垂直 Box 的 `BoxLayout` 轴、`Box(int)` 构造、`createRigidArea`/`createHorizontalStrut`/`createVerticalStrut` 的 preferred size、胶水组件非空。
