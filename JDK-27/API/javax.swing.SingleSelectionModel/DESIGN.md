# javax.swing.SingleSelectionModel 设计案

## 1. API 功能
`SingleSelectionModel` 接口描述单选模型。方法分组：
- 选择：`setSelectedIndex/clearSelection/isSelected/getSelectedIndex`。
- 监听：`addChangeListener/removeChangeListener`。

## 2. Java 用户如何使用
`JTabbedPane`/`JMenuBar` 使用该模型管理单选：
```java
SingleSelectionModel model = new DefaultSingleSelectionModel();
model.setSelectedIndex(0);
```
典型链路：`setSelectedIndex` → `getSelectedIndex/isSelected` → `clearSelection`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/DefaultSingleSelectionModel.java` 实现该接口（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultSingleSelectionModel.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 通过 `DefaultSingleSelectionModel` 覆盖初始 -1、`setSelectedIndex`、`isSelected`、`clearSelection`。
