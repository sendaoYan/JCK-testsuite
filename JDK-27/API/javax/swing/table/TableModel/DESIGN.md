# TableModel 测试设计

## 1. API 功能
`javax.swing.table.TableModel` 接口定义表格数据源契约。核心方法：

- **结构**：`getRowCount`、`getColumnCount`、`getColumnName`、`getColumnClass`。
- **单元格**：`getValueAt`、`setValueAt`、`isCellEditable`。
- **监听器**：`addTableModelListener`、`removeTableModelListener`。

## 2. Java 用户如何使用
- 实现：匿名类提供 backing 数组与监听列表，实现 9 个方法。
- 每个方法典型调用：`m.getRowCount()`；`m.getColumnCount()`；`m.getColumnName(0)`；`m.getColumnClass(0)`；`m.isCellEditable(r,c)`；`m.getValueAt(r,c)`；`m.setValueAt(v,r,c)`；`m.addTableModelListener(l)`；`m.removeTableModelListener(l)`。
- 真实组合链路：`getColumnCount → getColumnClass → getValueAt → setValueAt → getValueAt`。

## 3. 开源代码实际用法调研
1. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/AbstractTableModel.java`：实现该接口并补齐默认方法（本测试接口实现的对照）。
2. `test/jdk/javax/swing/JTableHeader/TableHeaderRendererTest.java` 的 `SampleTableModel extends AbstractTableModel` 展示最小编程模型。
3. SwingSet3 `OscarTableModel` 通过实现/继承该接口提供电影数据表。

## 4. 测试用例设计 + 自动化实现
落入 `TableModelTest.java` 的 `testBlackBox()`：
- 匿名实现，断言行列数、列名、列类（Integer/String）、isCellEditable、getValueAt。
- `setValueAt → getValueAt` 往返；监听器 add/remove 可调用。
共 18 个断言。
