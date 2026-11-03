# javax.swing.CellEditor 设计案

## 1. API 功能
`CellEditor` 是表格/树单元格编辑器接口。方法分组：
- 值：`getCellEditorValue()`。
- 查询：`isCellEditable(EventObject)`、`shouldSelectCell(EventObject)`。
- 生命周期：`stopCellEditing()`、`cancelCellEditing()`。
- 监听：`addCellEditorListener`、`removeCellEditorListener`。

## 2. Java 用户如何使用
用户实现它作为可编辑单元格的编辑器，编辑结束提交值：
```java
CellEditor ed = new CellEditor() {
    public Object getCellEditorValue() { return field.getText(); }
    ...
};
if (ed.stopCellEditing()) { table.setValue(ed.getCellEditorValue()); }
```
典型链路：`isCellEditable` → `shouldSelectCell` → `getCellEditorValue` → `stopCellEditing/cancelCellEditing`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/DefaultCellEditor.java` 实现 `CellEditor`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultCellEditor.java`）。
- JDK 源码 `javax/swing/AbstractCellEditor.java` 提供 `CellEditor` 的基础实现。
- JDK 源码 `javax/swing/JTable.java` 调用 `CellEditor` 管理编辑生命周期。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 isInterface、无父接口。
- 行为（黑盒）：`testBlackBox()` 用匿名实现覆盖全部 7 个方法，验证 `getCellEditorValue/isCellEditable/shouldSelectCell/stopCellEditing/cancelCellEditing` 的可调用与返回值。
