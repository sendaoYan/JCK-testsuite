# javax.swing.table.DefaultTableModel 测试设计

## 1. API 功能
Vector 驱动的具体 TableModel：6 个构造器（含行列数、列名 Vector/Object[]）、行/列增删、
`getDataVector/setDataVector`（活引用）、moveRow、setRowCount/setColumnCount、
`addColumn` 三个重载、`isCellEditable` 默认 true、`getColumnClass` 默认 Object.class。

## 2. Java 用户使用
`new DefaultTableModel(new Object[][]{{...}}, new Object[]{"A","B"})` 快速建模型；
`addRow(obj)` 追加、`setValueAt` 更新并投递 UPDATE、`moveRow(start,end,to)` 重排、
`setRowCount(0)` 清空（HEADER_ROW 结构事件由 setColumnIdentifiers 等触发）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/DefaultTableModel.java`；
`test/jdk/javax/swing/JTable/` 模型相关测试。

## 4. 测试用例设计 + 易错细节
- 六构造器矩阵：行列数、默认列名 A/B/C（spreadsheet letters）、初始 null 单元格。
- 行操作链 addRow→insertRow→removeRow 的值偏移；moveRow 的 (start,end,to) 语义与重叠；
  getValueAt/setValueAt/insertRow/removeRow 越界与 moveRow 目标超界均抛 ArrayIndexOutOfBoundsException。
- getDataVector 是活视图（改 Vector 元素立即可见）；setDataVector 换存储并投递 HEADER_ROW 结构事件。
- setValueAt 投递 UPDATE(first=last=row, column)；addRow 投递 INSERT(first=last=newRow)；
  removeRow 投递 DELETE(first=last=row)。
- addColumn 三重载（仅名/名+值/名+类）扩展列并投递结构事件；setColumnCount 收缩丢弃数据。
- 序列化 round-trip 后数据与结构保留。
- 版本差异：JDK-8 `getDataVector()` 返回 raw `Vector`（unchecked 赋值），JDK-9+ 声明为
  `Vector<Vector>`（赋给 `Vector<Vector<Object>>` 变为编译错误，两侧测试按各自签名书写）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
