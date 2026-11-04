# javax.swing.table.TableRowSorter 测试设计

## 1. API 功能
`RowSorter<TableModel>` 的具体实现：按列 Comparator 集合、字符串包装（tableStringConverter）、
setSortable/setSortKey(s)/toggleSortOrder（三态循环 ASC→DESC→无）、行过滤 RowFilter、
模型行↔视图行映射（convertRowIndexToView/Model）。

## 2. Java 用户如何使用
`TableRowSorter<TableModel> s = new TableRowSorter<>(model); table.setRowSorter(s);`
点击表头触发 toggleSortOrder；`s.setRowFilter(RowFilter.regexFilter("a"))` 过滤；
`convertRowIndexToModel(viewIdx)` 在自定义渲染器里取真实行。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/TableRowSorter.java`；
`test/jdk/javax/swing/JTable/` 排序测试（bug6702218 等）。

## 4. 测试用例设计 + 易错细节
- 构造：无参后 setModel；带模型构造。无排序键时 convertRowIndex 是恒等。
- toggleSortOrder 三态：首次 ASC、再 DESC、第三次清空（getSortKeys 空且映射恒等）；
  setSortKeys 指定 (column, ASC/DESC) 后视图顺序断言（Comparator 缺省按 toString 比较）。
- 每列 Comparator：setComparator(1, reverseComparator) 后顺序反转；getComparator 回读。
- RowFilter：过滤后 getViewRowCount<getModelRowCount、convertRowIndexToModel 跳过被滤行、
  convertRowIndexToView 对被滤行返回 -1。
- 事件：sorterChanged (RowSorterEvent type SORTED / SORT_ORDER_CHANGED)。
- 易错点：setSortKeys(null) 清空；getSortKeys() 返回**不可修改列表**（add 抛 UnsupportedOperationException）；
  setSortKeys 非法列/含 null 元素抛 IllegalArgumentException；不可排序列 toggle 无操作；
  toggleSortOrder 非法列与 convertRowIndexToView/Model 越界抛 IndexOutOfBoundsException。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
