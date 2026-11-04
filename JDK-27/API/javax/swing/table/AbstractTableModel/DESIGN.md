# javax.swing.table.AbstractTableModel 测试设计

## 1. API 功能
TableModel 的可序列化抽象骨架：实现监听器注册（EventListenerList）、fire* 事件分发（protected）、
列名默认规则（`A`..`Z`,`AA`..）、`findColumn`、`getRowCount/getColumnCount/getValueAt/isCellEditable/setValueAt`
的默认实现；子类只需实现三个抽象方法。

## 2. Java 用户如何使用
继承 `AbstractTableModel` 实现 `getRowCount/getColumnCount/getValueAt`，数据变更处调用
`fireTableCellUpdated/fireTableRowsInserted/fireTableRowsDeleted/fireTableRowsUpdated/fireTableDataChanged/fireTableStructureChanged`；
UI 侧 `addTableModelListener` 接收 TableModelEvent。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/AbstractTableModel.java`；
`test/jdk/javax/swing/JTable/` 各模型测试；`test/jdk/javax/swing/table/`。

## 4. 测试用例设计 + 易错细节
- 抽象性：类 abstract，恰好 3 个抽象方法（getRowCount/getColumnCount/getValueAt）；Serializable。
- 默认列名：0→A、25→Z、26→AA、27→AB、51→AZ、52→BA；负数返回空串（不抛异常）。
- `findColumn` 线性匹配列名，未命中 -1。
- `isCellEditable` 默认 false，`setValueAt` 默认空操作，`getColumnClass` 默认 Object.class。
- fire* 事件载荷精确断言（Recorder 记录 type/firstRow/lastRow/column）：
  INSERT/UPDATE/DELETE 行区间、列级 UPDATE 的 (first==last==row, column)、
  `fireTableDataChanged` 全区间 (0..Integer.MAX_VALUE)、`fireTableStructureChanged` HEADER_ROW/-1。
- 监听器注册表：add/remove 后 `getTableModelListeners`/`getListeners` 数量、移除后不再投递、
  无监听器时 fire 不抛异常。
- 序列化 round-trip 后行为保留（行列数/取值/isCellEditable=false/findColumn）。
- 易错点：TableModelEvent 构造中 firstRow>lastRow 表示 DELETE；HEADER_ROW==-1、ALL_COLUMNS==-1。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
