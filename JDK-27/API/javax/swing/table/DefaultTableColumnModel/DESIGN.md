# DefaultTableColumnModel 测试设计

## 1. API 功能
`javax.swing.table.DefaultTableColumnModel` 是 `TableColumnModel` 的标准实现，用 `Vector<TableColumn>` 维护列顺序，并实现 `PropertyChangeListener`/`ListSelectionListener`/`Serializable`。核心分组：

- **列维护**：`addColumn`、`removeColumn`、`moveColumn`、`getColumnCount`、`getColumns`（Enumeration 有序）、`getColumn(int)`、`getColumnIndex(Object identifier)`。
- **几何/边距**：`set/getColumnMargin`、`getColumnIndexAtX`、`getTotalColumnWidth`。
- **选择**：`set/getColumnSelectionAllowed`、`set/getSelectionModel`、`getSelectedColumns`、`getSelectedColumnCount`。
- **监听器**：`add/removeColumnModelListener`、`getColumnModelListeners`、`getListeners`。

## 2. Java 用户如何使用
- 构造：`DefaultTableColumnModel cm = new DefaultTableColumnModel();`
- 每个方法典型调用：`cm.addColumn(col)`；`cm.getColumn(i)`；`cm.getColumnIndex("id")`；`cm.moveColumn(from, to)`；`cm.removeColumn(col)`；`cm.setColumnMargin(5)`；`cm.getColumnIndexAtX(x)`；`cm.setColumnSelectionAllowed(true)`。
- 真实组合链路：`addColumn(a)→addColumn(b)→addColumn(c)→getColumnIndex("c")→moveColumn(2,0)→getColumn(0)==c`；`addColumn×2→getTotalColumnWidth→getColumnIndexAtX`。

## 3. 开源代码实际用法调研
1. SwingSet3 `test/jdk/sanity/client/lib/SwingSet3/src/com/sun/swingset3/demos/table/TableDemo.java`：`new DefaultTableColumnModel()` 后逐列 `addColumn(column)` 组装列模型，再挂到表格。
2. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/DefaultTableColumnModel.java`：`addColumn` 内部 `aColumn.addPropertyChangeListener(this)` 并 `fireColumnAdded(new TableColumnModelEvent(this, 0, getColumnCount()-1))`；`moveColumn` 先 `removeElementAt` 再 `insertElementAt` 并 `fireColumnMoved`（本测试监听器断言依据）。
3. `test/jdk/javax/swing/JTableHeader/*` 系列用例（如 `bug4243927.java`）通过 `JTableHeader` 间接使用列模型的 `addColumn`/`getColumn`。

## 4. 测试用例设计 + 自动化实现
落入 `DefaultTableColumnModelTest.java` 的 `testBlackBox()`：
- 空模型初始态（count 0、margin 1、选择关闭）。
- addColumn/getColumn/getColumnIndex/getColumns 顺序；moveColumn 后重新断言 `getColumnIndex` 与 `getColumn(0..2)` 顺序；removeColumn。
- `getColumnIndex` 未找到 / null 标识抛 `IllegalArgumentException`。
- 宽度工具：`getTotalColumnWidth` 求和、`getColumnIndexAtX` 边界（负值、列内、跨列、越界）。
- margin/选择开关、选择模型非空往返。
- `TableColumnModelListener`：addColumn/removeColumn/moveColumn/setColumnMargin 触发对应回调并断言事件 from/to 索引；移除监听后停止投递。
共 49 个断言。
