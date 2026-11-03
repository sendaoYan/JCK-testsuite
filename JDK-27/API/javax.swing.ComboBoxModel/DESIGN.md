# javax.swing.ComboBoxModel 设计案

## 1. API 功能
`ComboBoxModel<E>` 继承 `ListModel<E>`，增加选中项概念。方法分组：
- 选中项：`setSelectedItem(Object)`、`getSelectedItem()`。
- 继承自 `ListModel`：`getSize()`、`getElementAt(int)`。

## 2. Java 用户如何使用
用户实现/使用它作为下拉框的数据模型：
```java
ComboBoxModel<String> m = new DefaultComboBoxModel<>(new String[]{"a", "b", "c"});
m.setSelectedItem("b");
String selected = (String) m.getSelectedItem();
```
典型链路：`getSize/getElementAt` → `setSelectedItem` → `getSelectedItem`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/DefaultComboBoxModel.java` 实现 `ComboBoxModel`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultComboBoxModel.java`）。
- JDK 源码 `javax/swing/JComboBox.java` 通过 `ComboBoxModel` 管理下拉数据与选中项。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 isInterface、父接口 `ListModel`。
- 行为（黑盒）：`testBlackBox()` 用 `DefaultComboBoxModel` 验证 `getSize/getElementAt`、默认无选中、`setSelectedItem/getSelectedItem`。
