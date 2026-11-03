# TableRowSorter 测试设计

## 1. API 功能
`javax.swing.table.TableRowSorter<M extends TableModel>` 继承 `DefaultRowSorter<M,Integer>`，为 `TableModel` 提供排序与过滤。核心分组：

- **构造/模型**：`()`、`(M model)`、`setModel`、`getModel`（继承）。
- **排序**：`setSortKeys`、`getSortKeys`、`toggleSortOrder`、`sort`、`setComparator`/`getComparator`、`setSortable`/`isSortable`、`setMaxSortKeys`、`setSortsOnUpdates`。
- **过滤**：`setRowFilter`、`getRowFilter`。
- **视图/模型换算**：`convertRowIndexToView`、`convertRowIndexToModel`、`getViewRowCount`、`getModelRowCount`。
- **字符串转换**：`setStringConverter`、`getStringConverter`。

## 2. Java 用户如何使用
- 构造：`TableRowSorter<DefaultTableModel> s = new TableRowSorter<>(model);`
- 每个方法典型调用：`s.setComparator(0, Comparator.naturalOrder())`；`s.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)))`；`s.sort()`；`s.toggleSortOrder(0)`；`s.convertRowIndexToModel(i)`；`s.setSortable(0,false)`；`s.setRowFilter(RowFilter.regexFilter("x",0))`；`s.setStringConverter(c)`。
- 真实组合链路：`new TableRowSorter(model) → setComparator → setSortKeys → sort → convertRowIndexToModel(viewIdx) → model.getValueAt(...)`。

## 3. 开源代码实际用法调研
1. `test/jdk/javax/swing/JTable/DefaultRowSorterCacheTest.java`：`TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(model); rowSorter.toggleSortOrder(0);` 并用 `convertRowIndexToView(row)` 校验排序；其 `CustomTableModel` 覆盖 `getColumnClass` 返回单元格实际类型（Integer）以启用数值排序——本测试以 `setComparator` 达到同样的确定性数值排序。
2. SwingSet3 `test/jdk/sanity/client/lib/SwingSet3/src/com/sun/swingset3/demos/table/TableDemo.java`：`sorter = new TableRowSorter(oscarModel); oscarTable.setRowSorter(sorter);` 并自定义 `RowFilter` 过滤获奖者。
3. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/TableRowSorter.java`：文档明确 `DefaultTableModel` 返回 `Object` 列类，故默认用 `toString` 比较，需覆盖 `getColumnClass` 或 `setComparator` 避免字符串排序（权威依据）。

## 4. 测试用例设计 + 自动化实现
落入 `TableRowSorterTest.java` 的 `testBlackBox()`：
- 用 Integer 列 + `setComparator(0, naturalOrder())` 强制数值排序（规避 locale 字符串排序），`setSortKeys→sort` 后断言视图到模型映射为 [1,2,0] 与取值。
- 排序后 `convertRowIndexToView(convertRowIndexToModel(i)) == i` 逐行往返；`getSortKeys` 内容；`toggleSortOrder` 反转。
- `setSortable/isSortable`、`set/getStringConverter` 往返、`getViewRowCount/getModelRowCount`。
- 新 sorter + `setRowFilter(RowFilter.regexFilter("1|3",0))`：视图行数 2、被过滤行 `convertRowIndexToView==-1`。
- `set/getMaxSortKeys`、`set/getSortsOnUpdates`。
共 40 个断言。
