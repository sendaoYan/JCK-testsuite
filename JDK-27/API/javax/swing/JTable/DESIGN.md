# javax.swing.JTable — DESIGN

## 1. API 功能
表格（`extends JComponent`，implements TableModelListener/Scrollable/TableColumnModelListener/ListSelectionListener/CellEditorListener/Accessible/RowSorterListener）。核心方法分组：
- 模型：`setModel/getModel`（TableModel）、`getValueAt/setValueAt`（view 坐标，内部 convertRowIndexToModel）、`getRowCount/getColumnCount/getColumnName/getColumnClass/isCellEditable`。
- 选择：`setRowSelectionInterval/getSelectedRow/getSelectedRows/getSelectedRowCount/isRowSelected`、`clearSelection`、`changeSelection`、`setColumnSelectionAllowed/setRowSelectionAllowed`、列选择 getSelectedColumn/getSelectedColumns/getSelectedColumnCount/isColumnSelected/isCellSelected。
- 排序：`setRowSorter/getRowSorter`、`setAutoCreateRowSorter/getAutoCreateRowSorter`、`convertRowIndexToView/convertRowIndexToModel`、`getUpdateSelectionOnSort`。
- 列：`moveColumn`、`convertColumnIndexToView/convertColumnIndexToModel`、`getColumn(Object identifier)`（TableColumn）。
- 渲染/编辑：`getDefaultRenderer/setDefaultRenderer`、`getDefaultEditor/setDefaultEditor`。
- 几何：`setRowHeight/getRowHeight`、`setRowMargin/getRowMargin`、`setShowGrid/getShowHorizontalLines/getShowVerticalLines`、`setAutoResizeMode/getAutoResizeMode`。

## 2. Java 用户如何使用
`DefaultTableModel model = new DefaultTableModel(data, colNames);` → `new JTable(model)` → `setRowSorter(new TableRowSorter<>(model))` → `convertRowIndexToView` 建立排序映射 → `setRowSelectionInterval`/`getSelectedRow`；`setValueAt` 写回模型。注意 `getValueAt(row,col)` 用 view 坐标并转换到 model。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java`：`getValueAt` 调 `getModel().getValueAt(convertRowIndexToModel(row), convertColumnIndexToModel(column))`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JTable/`：排序后行/列 view-model 转换、moveColumn、选择区间断言。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/DefaultTableModel.java`：固定数据 + 可编辑单元格驱动。

## 4. 测试用例设计（黑盒部分，`JTableTest.testBlackBox`）
1. 固定 DefaultTableModel：行/列数、getValueAt、getColumnName、getColumnClass、isCellEditable。
2. 行选择 setRowSelectionInterval/getSelectedRow/getSelectedRows/isRowSelected/clearSelection。
3. TableRowSorter 排序：toggleSortOrder(0) 升序 convertRowIndexToView/ToModel 映射 + getValueAt 转换 + selection 跟随排序；降序再断言；setRowSorter(null) 恒等映射。
4. autoCreateRowSorter 安装/清空；moveColumn(0,1) 列 view-model 转换 + getColumnName 用 view 坐标 + getColumn(identifier).headerValue。
5. changeSelection 选中行列；列选择允许 + setRowSelectionInterval/setColumnSelectionInterval/getSelectedColumns/isCellSelected。
6. 默认 renderer/editor 非 null + 自定义 setDefaultRenderer 往返。
7. rowHeight/rowMargin/showGrid/autoResizeMode 往返；setValueAt 写回。
