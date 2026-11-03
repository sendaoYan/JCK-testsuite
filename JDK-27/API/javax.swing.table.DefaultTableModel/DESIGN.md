# DefaultTableModel 测试设计

## 1. API 功能
`javax.swing.table.DefaultTableModel` 是 `AbstractTableModel` 的标准实现，用 `Vector<Vector>` 存储行数据、用 `Vector<?>` 存储列标识，可直接用于 `JTable`。核心分组：

- **构造器**：`()` 空模型；`(int rowCount, int columnCount)` 生成 null 单元格与默认列名；`(Object[][] data, Object[] columnNames)`、`(Vector data, Vector columnNames)` 带数据；`(Object[] columnNames, int rowCount)`、`(Vector columnNames, int rowCount)` 只给列名。
- **行操作**：`addRow(Object[]/Vector)`、`insertRow(int, ...)`、`removeRow(int)`、`moveRow(start,end,to)`、`setRowCount`、`setNumRows`。
- **列操作**：`addColumn(Object)`、`addColumn(Object,Object[]/Vector)`、`setColumnIdentifiers(...)`、`setColumnCount`。注意：JDK-27 该类**没有** `removeColumn` 和 `getColumnIdentifiers`（javap 确认）。
- **查询/更新**：`getRowCount`/`getColumnCount`/`getValueAt`/`setValueAt`/`getColumnName`/`isCellEditable`/`getDataVector`/`setDataVector`。
- **事件**：继承 `AbstractTableModel` 的 `fireTable*` 系列，及 `newDataAvailable`/`newRowsAdded`/`rowsRemoved`。

## 2. Java 用户如何使用
- 构造：`DefaultTableModel m = new DefaultTableModel(new Object[][]{{1,"a"}}, new Object[]{"ID","Name"});`
- 每个方法典型调用：`m.addRow(new Object[]{2,"b"})`；`m.insertRow(0, new Object[]{0,"z"})`；`m.removeRow(1)`；`m.addColumn("Extra")`；`m.setColumnIdentifiers(new Object[]{...})`；`m.getValueAt(0,0)`；`m.setValueAt("A",0,1)`；`m.setDataVector(...)`；`m.getDataVector()`。
- 真实组合链路：`new DefaultTableModel(data, cols) → addRow → setValueAt → getValueAt → removeRow → getRowCount`；或 `addColumn(name, data[]) → getColumnName(newIndex) → getValueAt(row,newIndex)`。

## 3. 开源代码实际用法调研
1. JDK 自带测试 `test/jdk/javax/swing/JTable/DefaultRowSorterCacheTest.java`：`new DefaultTableModel(...)` 后子类覆盖 `getColumnClass` 返回单元格实际类型，配合 `TableRowSorter` 做数值排序（这正是本测试 TableRowSorter 采用 `setComparator` 强制数值排序的原因）。
2. SwingSet3 演示 `test/jdk/sanity/client/lib/SwingSet3/src/com/sun/swingset3/demos/table/TableDemo.java`：`OscarTableModel extends DefaultTableModel`，通过 `createColumnModel()` 用 `TableColumn` + `setPreferredWidth` 组织表格，配合 `TableRowSorter`。
3. 多处 `test/jdk` 用例（`JComboBox/bug4890345.java`、`JInternalFrame/Test6505027.java` 等）直接用 `new DefaultTableModel(...)` 填充表格数据再挂到组件上。

## 4. 测试用例设计 + 自动化实现
从 1–3 推导场景，落入 `DefaultTableModelTest.java` 的 `testBlackBox()`：
- 六个构造器的行列数/列名/单元格值断言（含默认列名 "A"/"B"、`findColumn`）。
- 行/列增删改的组合链路：`addRow→insertRow→removeRow` 校验值偏移；`addColumn` 三个重载 + `setColumnIdentifiers` 两个重载 + `setColumnCount` 收缩。
- `getDataVector`/`setDataVector`、`setValueAt`/`getValueAt`、`setRowCount`、`moveRow` 的往返与顺序。
- `TableModelListener` 触发：`addRow`→INSERT、`setValueAt`→UPDATE（含 firstRow/column 定位）、`removeRow`→DELETE、`fireTableStructureChanged`→HEADER_ROW、`fireTableDataChanged`→全区间，以及移除监听后不再投递。
共 72 个断言。
