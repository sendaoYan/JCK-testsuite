# javax.swing.table.DefaultTableColumnModel 测试设计

## 1. API 功能
TableColumnModel 默认实现：列集合（Vector）+ 列选择（DefaultListSelectionModel）+ 列边距，
addColumn/removeColumn/moveColumn/getColumnIndex/getColumn、列监听器与选择监听器分发。

## 2. Java 用户如何使用
JTable 内部持有；程序侧 `addColumn(new TableColumn(0))`、`moveColumn(0,2)` 重排、
`getColumn(getColumnIndex(identifier))` 取列；`setColumnMargin` 调整列间距。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/DefaultTableColumnModel.java`；
JTable.java；`test/jdk/jang/swing` 下的 JTable 测试。

## 4. 测试用例设计 + 易错细节
- 默认：无列、columnMargin=1、selectionAllowed、getTotalColumnWidth=0、选择模型非 null。
- addColumn 首列时把选择扩到该列；addColumn 重复对象不去重。
- moveColumn 语义（both endpoints）：moveColumn(0,2) 后顺序断言；越界端点抛 IllegalArgumentException，
  getColumn 越界/负数抛 ArrayIndexOutOfBoundsException。
- removeColumn 不存在的列无操作；remove 后列索引/总宽刷新。
- setColumnMargin 投递 margin 变更；getTotalColumnWidth= sum(width)+margin*列数。
- 列选择：getSelectedColumn(s) 委托选择模型；setColumnSelectionAllowed 影响事件。
- 序列化 round-trip 后列数/标识符/选择保留。
- 易错点：getColumnIndex(null) 抛 IllegalArgumentException（非 NPE）、未命中抛 IllegalArgumentException；
  addColumn(null)/setSelectionModel(null) 均抛 IllegalArgumentException；identifier 为 null 的列以 headerValue 参与。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
