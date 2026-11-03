# javax.swing.DefaultComboBoxModel 设计案

## 1. API 功能
`DefaultComboBoxModel<E>` 是 `MutableComboBoxModel<E>` 的默认实现，用 `Vector` 存元素并维护选中项。方法分组：
- 构造：`DefaultComboBoxModel()`、`DefaultComboBoxModel(E[])`、`DefaultComboBoxModel(Vector<E>)`。
- 查询：`getSize/getElementAt/getIndexOf/getSelectedItem`。
- 修改：`addElement`、`insertElementAt`、`removeElementAt`、`removeElement`、`removeAllElements`、`addAll`。
- 选中：`setSelectedItem/getSelectedItem`。

## 2. Java 用户如何使用
用户把它作为下拉框的数据模型，动态增删元素：
```java
DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>(new String[]{"a", "b"});
m.addElement("c");
m.setSelectedItem("c");
```
典型链路：`new DefaultComboBoxModel(array)` → `addElement` → `insertElementAt` → `setSelectedItem` → `getSelectedItem`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JComboBox.java` 默认使用 `DefaultComboBoxModel`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComboBox.java`）。
- JDK 测试 `test/jdk/javax/swing/JComboBox/` 大量使用 `DefaultComboBoxModel`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractListModel`、实现 `MutableComboBoxModel`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖数组/Vector/空三种构造、`getSize/getElementAt/getIndexOf`、`setSelectedItem`、`addElement/insertElementAt/removeElement/removeElementAt/addAll`。
