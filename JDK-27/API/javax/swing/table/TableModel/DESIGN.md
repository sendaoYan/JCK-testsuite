# javax.swing.table.TableModel 测试设计

## 1. API 功能
表格数据最小契约接口：9 个方法（getRowCount/getColumnCount/getValueAt/setValueAt/getColumnName/
getColumnClass/isCellEditable/addTableModelListener/removeTableModelListener）。

## 2. Java 用户如何使用
JTable 构造时接收 TableModel；自定义模型实现该接口（或继承 AbstractTableModel 省事），
监听器收到 TableModelEvent 后按 type/区间重绘。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/TableModel.java`；
JTable.java 内部调用点；`test/jdk/javax/swing/table/`。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、无父接口之外的成员、全部 public abstract，恰好 9 个方法名。
- 已知实现：AbstractTableModel（abstract class）、DefaultTableModel 直接可用。
- 多态：RecordingModel（手写实现）注册到 DefaultTableModel 的对照 + 通过接口引用驱动
  RecordingModel 的 getValueAt/getColumnName/isCellEditable 与监听器投递。
- 易错点：接口本身不带 TableModelListener 事件类型常量（常量在 TableModelEvent）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
