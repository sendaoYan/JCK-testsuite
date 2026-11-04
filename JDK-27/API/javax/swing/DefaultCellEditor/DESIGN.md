# javax.swing.DefaultCellEditor 设计案

## 1. API 功能
`DefaultCellEditor` 是 `AbstractCellEditor` 的具体实现，用 JTextField/JCheckBox/JComboBox 作为表格/树单元格编辑器。方法分组：
- 构造：`DefaultCellEditor(JTextField|JCheckBox|JComboBox)`。
- 组件：`getComponent()`、`getTreeCellEditorComponent(JTree,...)`、`getTableCellEditorComponent(JTable,...)`。
- 配置：`setClickCountToStart/getClickCountToStart`。
- 编辑：`getCellEditorValue`、`isCellEditable`、`shouldSelectCell`、`stopCellEditing`、`cancelCellEditing`。

## 2. Java 用户如何使用
用户把它装到表格列上，双击进入编辑：
```java
JTextField field = new JTextField();
DefaultCellEditor ed = new DefaultCellEditor(field);
table.getColumnModel().getColumn(0).setCellEditor(ed);
```
典型链路：`new DefaultCellEditor(field)` → `getComponent` → `setClickCountToStart` → `getCellEditorValue` → `stopCellEditing`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JTable.java` 用 `DefaultCellEditor` 作为默认布尔/文本编辑器（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java`）。
- JDK 源码 `javax/swing/JTree.java` 同样使用 `DefaultCellEditor`。
- JDK 测试 `test/jdk/javax/swing/JTable/`、`JTree/` 覆盖默认单元格编辑。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractCellEditor`、实现 `TableCellEditor`+`TreeCellEditor`。
- 行为（黑盒）：`testBlackBox()` 覆盖三种构造器、`getComponent`、文本/复选框/下拉框的 `getCellEditorValue`、`setClickCountToStart/getClickCountToStart`、`isCellEditable/shouldSelectCell/stopCellEditing`。
