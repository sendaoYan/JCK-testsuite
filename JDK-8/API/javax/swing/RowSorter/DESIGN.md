# javax.swing.RowSorter 测试设计

## 1. API 功能
排序骨架（abstract，17 个方法名/14 抽象）：SortKey 列表管理（set/getSortKeys、toggleSortOrder）、
模型↔视图行映射（convertRowIndexToView/Model、getViewRowCount/getModelRowCount）、
行结构变化通知（rowsInserted/Deleted/Updated/StructureChanged）、sorter 监听器（protected fire*）。

## 2. Java 用户如何使用
JTable.setRowSorter(rowSorter) 驱动视图；自定义子类实现映射逻辑，
调 protected fireRowSorter/sorterChanged 通知表格；SortKey(column, ASCENDING/DESCENDING) 不可变。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/RowSorter.java`；
TableRowSorter 子类；JTable 调用点。

## 4. 测试用例设计 + 易错细节
- 反射形态：abstract 类、17 个方法名、其中 14 个抽象；fire* 均为 protected；
  SortKey 为不可变 final（构造后 getColumn/getSortOrder）。
- SortKey.SORT_ORDER 语义：ASCENDING/DESCENDING/UNSORTED 互异；null sortKeys 列表转空。
- 子类（RecordingRowSorter）经父类引用驱动：convertRowIndexToView/Model 回调记录、
  addRowSorterListener 后 fireRowSorter(RowSorterEvent.SORTED/SORT_ORDER_CHANGED) 投递、
  移除监听后静默。
- 易错点：RowSorter 无 getRowSorterListeners()（与 JTable 不同），监听器需自行跟踪。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
