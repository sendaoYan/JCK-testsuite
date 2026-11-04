# TableColumnModel 测试设计

## 1. API 功能
`javax.swing.table.TableColumnModel` 接口定义列集合的顺序、边距、选择与监听。核心方法：

- **列维护**：`addColumn`、`removeColumn`、`moveColumn`、`getColumnCount`、`getColumns`、`getColumn(int)`、`getColumnIndex(Object)`。
- **几何**：`set/getColumnMargin`、`getColumnIndexAtX`、`getTotalColumnWidth`。
- **选择**：`set/getColumnSelectionAllowed`、`set/getSelectionModel`、`getSelectedColumns`、`getSelectedColumnCount`。
- **监听器**：`add/removeColumnModelListener`。

## 2. Java 用户如何使用
- 实现：通常用 `DefaultTableColumnModel`；接口视角 `TableColumnModel m = new DefaultTableColumnModel();`
- 每个方法典型调用：`m.addColumn(c)`；`m.getColumn(i)`；`m.getColumnIndex(id)`；`m.moveColumn(a,b)`；`m.setColumnMargin(5)`；`m.getColumnIndexAtX(x)`；`m.setColumnSelectionAllowed(true)`；`m.addColumnModelListener(l)`。
- 真实组合链路：`addColumn×2 → getColumnCount → getTotalColumnWidth → getColumnIndexAtX`。

## 3. 开源代码实际用法调研
1. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/DefaultTableColumnModel.java`：作为该接口的标准实现（本测试的匿名实现即委托给它，逐方法验证接口契约）。
2. SwingSet3 `test/jdk/sanity/client/lib/SwingSet3/src/com/sun/swingset3/demos/table/TableDemo.java`：`DefaultTableColumnModel columnModel = new DefaultTableColumnModel(); ... columnModel.addColumn(column);`。

## 4. 测试用例设计 + 自动化实现
落入 `TableColumnModelTest.java` 的 `testBlackBox()`：
- 匿名实现委托 `DefaultTableColumnModel`，逐方法调用并断言：初始计数/边距、addColumn/getColumn/getColumnIndex/getColumns 顺序、getTotalColumnWidth、getColumnIndexAtX、moveColumn 重排、set/getColumnMargin、set/getColumnSelectionAllowed、选择模型往返、removeColumn、addColumnModelListener 可调用。
共 25 个断言。
