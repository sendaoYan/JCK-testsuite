# javax.swing.DefaultRowSorter 设计案

## 1. API 功能
`DefaultRowSorter<M, I>` 是 `RowSorter<M>` 的抽象基类，提供基于列的排序/过滤的通用实现。方法分组：
- 模型：`getModel()`、`getModelRowCount/getViewRowCount`。
- 排序：`setSortable/isSortable`、`setSortKeys/getSortKeys`、`toggleSortOrder`、`sort`、`setComparator/getComparator`、`useToString`。
- 行映射：`convertRowIndexToView/convertRowIndexToModel`。
- 配置：`setMaxSortKeys/getMaxSortKeys`、`setSortsOnUpdates/getSortsOnUpdates`、`setRowFilter/getRowFilter`。
- 模型事件：`modelStructureChanged/allRowsChanged/rowsInserted/rowsDeleted/rowsUpdated`。

## 2. Java 用户如何使用
用户使用具体子类 `TableRowSorter`/`ListRowSorter` 给表格/列表排序：
```java
TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
sorter.toggleSortOrder(0);
sorter.sort();
table.setRowSorter(sorter);
```
典型链路：`new TableRowSorter(model)` → `toggleSortOrder` → `sort` → `convertRowIndexToModel/View` → `getSortKeys`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/table/TableRowSorter.java` 与 `javax/swing/DefaultRowSorter.java` 同模块，前者继承后者（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/TableRowSorter.java`）。
- JDK 源码 `javax/swing/JTable.java` 的 `setRowSorter` 使用 `DefaultRowSorter` 语义。
- JDK 测试 `test/jdk/javax/swing/JTable/` 覆盖排序与行映射。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、父类 `RowSorter`。
- 行为（黑盒）：`testBlackBox()` 用 `TableRowSorter` 覆盖 `getModel/getModelRowCount/getViewRowCount/getMaxSortKeys/getSortsOnUpdates/isSortable`、`setSortable`、`toggleSortOrder+sort` 后排序键与 view->model 映射、`setMaxSortKeys/setSortsOnUpdates`、`convertRowIndexToView/Model` 往返。
