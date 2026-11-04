# javax.swing.ComboBoxEditor 设计案

## 1. API 功能
`ComboBoxEditor` 是下拉框编辑器接口。方法分组：
- 组件：`getEditorComponent()`。
- 值：`setItem(Object)`、`getItem()`。
- 选择：`selectAll()`。
- 监听：`addActionListener`、`removeActionListener`。

## 2. Java 用户如何使用
用户实现它作为可编辑下拉框的文本输入组件：
```java
JComboBox<Object> box = new JComboBox<>(...);
box.setEditable(true);
ComboBoxEditor ed = box.getEditor();
ed.setItem("current");
Object value = ed.getItem();
```
典型链路：`getEditorComponent` → `setItem` → `getItem` → `selectAll` → `addActionListener`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicComboBoxEditor.java` 实现 `ComboBoxEditor`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicComboBoxEditor.java`）。
- JDK 源码 `javax/swing/JComboBox.java` 通过 `ComboBoxEditor` 管理可编辑模式文本。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 isInterface、无父接口。
- 行为（黑盒）：`testBlackBox()` 用匿名实现覆盖全部 6 个方法，验证 `getEditorComponent/setItem/getItem/selectAll/addActionListener/removeActionListener` 的可调用性。
