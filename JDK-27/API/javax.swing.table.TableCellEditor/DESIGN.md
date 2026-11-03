# TableCellEditor 测试设计

## 1. API 功能
`javax.swing.table.TableCellEditor` 继承 `javax.swing.CellEditor`，用于单元格编辑。核心方法：

- **编辑器组件**：`getTableCellEditorComponent(JTable, value, isSelected, row, column)`。
- **值/生命周期**（继承 CellEditor）：`getCellEditorValue`、`isCellEditable(EventObject)`、`shouldSelectCell(EventObject)`、`stopCellEditing`、`cancelCellEditing`。
- **监听器**（继承 CellEditor）：`addCellEditorListener`、`removeCellEditorListener`。

## 2. Java 用户如何使用
- 实现：匿名类同时实现上述 7 个方法。
- 每个方法典型调用：`editor.getTableCellEditorComponent(table, "v", true, 0, 0)`；`editor.getCellEditorValue()`；`editor.isCellEditable(evt)`；`editor.shouldSelectCell(evt)`；`editor.stopCellEditing()`；`editor.cancelCellEditing()`；`editor.addCellEditorListener(l)`。
- 真实组合链路：`getTableCellEditorComponent → 编辑 → stopCellEditing → editingStopped 监听 → getCellEditorValue`。

## 3. 开源代码实际用法调研
1. JDK 实现 `src/java.desktop/share/classes/javax/swing/DefaultCellEditor.java`：实现 `TableCellEditor`，`getTableCellEditorComponent` 返回编辑器组件，`stopCellEditing` 触发 `editingStopped`（本测试监听器行为参考）。
2. `test/jdk/javax/swing/JTable/*` 系列用例通过自定义编辑器实现该接口验证编辑流程。

## 4. 测试用例设计 + 自动化实现
落入 `TableCellEditorTest.java` 的 `testBlackBox()`：
- 匿名实现：`getTableCellEditorComponent` 返回带文本的 JLabel，`getCellEditorValue` 返回该值。
- `isCellEditable(null)`/非 null 事件、`shouldSelectCell`。
- `CellEditorListener`：`stopCellEditing` 触发 `editingStopped`、`cancelCellEditing` 触发 `editingCanceled`，移除监听后不再投递。
共 19 个断言。
