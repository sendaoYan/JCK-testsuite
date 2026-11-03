# AbstractTableModel 测试设计

## 1. API 功能
`javax.swing.table.AbstractTableModel` 是 `TableModel` 的抽象基类，实现监听器管理与默认方法，子类只需提供 `getRowCount`/`getColumnCount`/`getValueAt`。核心分组：

- **默认实现**：`getColumnName`（电子表格式 A、B、…、Z、AA、AB）、`findColumn`、`getColumnClass`（返回 `Object.class`）、`isCellEditable`（返回 false）、`setValueAt`（默认空操作）。
- **监听器**：`add/removeTableModelListener`、`getTableModelListeners`、`getListeners`。
- **事件触发**：`fireTableDataChanged`、`fireTableStructureChanged`、`fireTableRowsInserted/Updated/Deleted`、`fireTableCellUpdated`、`fireTableChanged`。

## 2. Java 用户如何使用
- 子类化：`class M extends AbstractTableModel { public int getRowCount(){...} public int getColumnCount(){...} public Object getValueAt(r,c){...} }`
- 每个方法典型调用：`m.getColumnName(0)`；`m.findColumn("B")`；`m.getColumnClass(0)`；`m.isCellEditable(r,c)`；`m.fireTableRowsInserted(0, n-1)`；`m.fireTableCellUpdated(r,c)`；`m.fireTableStructureChanged()`。
- 真实组合链路：`setValueAt → fireTableCellUpdated → 监听器收到 UPDATE`；`fireTableRowsInserted → 监听器收到 INSERT`。

## 3. 开源代码实际用法调研
1. `test/jdk/javax/swing/JTableHeader/TableHeaderRendererTest.java`：`static class SampleTableModel extends AbstractTableModel` 并实现 `getRowCount`/`getColumnCount`/`getValueAt`，是最小化子类范式。
2. 多个 `test/jdk` 用例（`JComboBox/bug4368848.java`、`JScrollPane/bug4244899.java`）通过 `extends AbstractTableModel` 提供表格数据源。
3. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/AbstractTableModel.java`：`getColumnName` 用 `(char)(column%26)+'A'` 循环生成，`fireTableDataChanged` 即 `fireTableChanged(new TableModelEvent(this))`（本测试列名与事件断言依据）。

## 4. 测试用例设计 + 自动化实现
落入 `AbstractTableModelTest.java` 的 `testBlackBox()`：
- 子类 `MyModel` 提供行列数与 `getValueAt`，断言行列数、取值。
- 默认 `getColumnName` 的 A/B/Z/AA/AB；`findColumn` 命中与未命中 -1。
- 默认 `getColumnClass`=Object、`isCellEditable`=false、`setValueAt` 覆盖后写入。
- `TableModelListener`：`fireTableCellUpdated`（UPDATE + 行/列定位）、`fireTableRowsUpdated/Inserted/Deleted`（类型与区间）、`fireTableStructureChanged`（HEADER_ROW）、`fireTableDataChanged`（全区间），以及 `add/remove/getTableModelListeners`、`getListeners`。
共 41 个断言。
