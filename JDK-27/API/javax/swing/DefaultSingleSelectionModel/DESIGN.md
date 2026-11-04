# javax.swing.DefaultSingleSelectionModel 设计案

## 1. API 功能
`DefaultSingleSelectionModel` 是 `SingleSelectionModel` 的默认实现，维护单一选中索引。方法分组：
- 查询：`getSelectedIndex()`、`isSelected()`。
- 修改：`setSelectedIndex(int)`、`clearSelection()`。
- 监听：`addChangeListener`、`removeChangeListener`、`getChangeListeners`、`getListeners(Class)`。

## 2. Java 用户如何使用
用户把它用于单选的组件（如 JTabbedPane）：
```java
SingleSelectionModel m = new DefaultSingleSelectionModel();
m.setSelectedIndex(0);
tabs.setModel(m);
```
典型链路：`new DefaultSingleSelectionModel` → `setSelectedIndex` → `getSelectedIndex/isSelected` → `clearSelection`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JTabbedPane.java` 默认使用 `DefaultSingleSelectionModel` 管理选中的 tab（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTabbedPane.java`）。
- JDK 源码 `javax/swing/JPopupMenu.java` 等同样使用 `SingleSelectionModel`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `SingleSelectionModel`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖默认 -1/未选中、`setSelectedIndex/isSelected`、`addChangeListener` 触发 `stateChanged`、`clearSelection`、监听计数。
