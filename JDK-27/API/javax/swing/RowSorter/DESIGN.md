# javax.swing.RowSorter 设计案

## 1. API 功能
`RowSorter` 抽象类为表格/列表排序。方法分组：
- 模型：`getModel/getModelRowCount/getViewRowCount`。
- 排序：`toggleSortOrder/setSortKeys/getSortKeys`。
- 索引转换：`convertRowIndexToModel/convertRowIndexToView`。

## 2. Java 用户如何使用
用户通过 `TableRowSorter` 排序表格：
```java
TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
sorter.toggleSortOrder(column);
int modelIndex = sorter.convertRowIndexToModel(viewIndex);
```
典型链路：`toggleSortOrder` → `getSortKeys` → `convertRowIndexToModel/View`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/table/TableRowSorter.java` 继承 `DefaultRowSorter`/`RowSorter`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/TableRowSorter.java`）。
- `RowSorter` 抽象方法由 `DefaultRowSorter` 实现。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract。
- 行为（黑盒）：`testBlackBox()` 通过 `TableRowSorter<DefaultTableModel>` 覆盖 `getModel`、行计数、`toggleSortOrder` 后升序排序与索引双向转换。
