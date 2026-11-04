# javax.swing.DefaultListSelectionModel 设计案

## 1. API 功能
`DefaultListSelectionModel` 是 `ListSelectionModel` 的默认实现，维护区间选择状态。方法分组：
- 查询：`getMinSelectionIndex/getMaxSelectionIndex/isSelectedIndex/isSelectionEmpty`。
- 区间：`setSelectionInterval/addSelectionInterval/removeSelectionInterval`。
- 锚点：`getAnchorSelectionIndex/getLeadSelectionIndex/setAnchorSelectionIndex/setLeadSelectionIndex/moveLeadSelectionIndex`。
- 模式：`setSelectionMode/getSelectionMode`。
- 其他：`setValueIsAdjusting/getValueIsAdjusting`、`clearSelection`、`clone`、`addListSelectionListener`。

## 2. Java 用户如何使用
用户把它绑到 JList/JTable 上管理选择：
```java
DefaultListSelectionModel m = new DefaultListSelectionModel();
m.setSelectionInterval(2, 4);
list.setSelectionModel(m);
```
典型链路：`new DefaultListSelectionModel` → `setSelectionInterval` → `getSelectedIndices` → `clearSelection` → `setSelectionMode`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JList.java` 默认使用 `DefaultListSelectionModel`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JList.java`）。
- JDK 源码 `javax/swing/JTable.java` 同样使用它管理行列选择。
- JDK 测试 `test/jdk/javax/swing/JList/`、`JTable/` 覆盖选择区间。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `ListSelectionModel`+`Cloneable`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖默认空选择/多区间模式、`setSelectionInterval` 后 min/max/isSelected、`getSelectedIndices/getSelectedItemsCount`、anchor/lead、`add/removeSelectionInterval`、`setValueIsAdjusting`、监听触发、`clone` 独立副本、单选模式。
