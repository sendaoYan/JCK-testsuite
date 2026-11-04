# javax.swing.GroupLayout 设计案

## 1. API 功能
`GroupLayout` 是基于水平/垂直 `Group` 的布局管理器。方法分组：
- 构造：`GroupLayout(Container)`。
- 组：`createSequentialGroup/createParallelGroup`、`setHorizontalGroup/setVerticalGroup`、`createBaselineGroup`。
- 配置：`setAutoCreateGaps/getAutoCreateGaps`、`setAutoCreateContainerGaps`、`setHonorsVisibility/getHonorsVisibility`。
- 其他：`linkSize`、`replace`、`setLayoutStyle/getLayoutStyle`、`layoutContainer`、`preferredLayoutSize` 等。

## 2. Java 用户如何使用
用户用它精确排布表单/对话框控件：
```java
GroupLayout layout = new GroupLayout(panel);
panel.setLayout(layout);
layout.setAutoCreateGaps(true);
layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(a).addComponent(b));
layout.setVerticalGroup(layout.createParallelGroup().addComponent(a).addComponent(b));
```
典型链路：`new GroupLayout(panel)` → `createSequentialGroup` → `setHorizontalGroup` → `linkSize` → `layoutContainer`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/GroupLayout.java` 是 JDK 自带实现（NetBeans 起源）（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/GroupLayout.java`）。
- JDK 测试 `test/jdk/javax/swing/GroupLayout/` 覆盖组布局行为。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `LayoutManager2`。
- 行为（黑盒）：`testBlackBox()` 覆盖自动空隙/容器空隙开关、honors visibility、`createSequentialGroup/createParallelGroup`、`setHorizontalGroup/setVerticalGroup`、`linkSize` 后两组件同宽、`layoutContainer` smoke。
