# JTableHeader 测试设计

## 1. API 功能
`javax.swing.table.JTableHeader` 继承 `JComponent`，实现 `TableColumnModelListener` 与 `Accessible`，渲染表格列头。核心分组：

- **构造/模型**：`()`、`(TableColumnModel)`、`set/getColumnModel`、`set/getTable`。
- **行为开关**：`set/getReorderingAllowed`、`set/getResizingAllowed`、`set/getUpdateTableInRealTime`。
- **拖拽/尺寸状态**：`getDraggedColumn`、`getDraggedDistance`、`getResizingColumn`、`setDraggedColumn`、`setDraggedDistance`、`setResizingColumn`。
- **渲染器**：`set/getDefaultRenderer`。
- **几何/UI**：`columnAtPoint`、`getHeaderRect`、`getPreferredSize`、`getToolTipText`、`getUI`/`setUI`、`updateUI`、`getUIClassID`、`getAccessibleContext`、`resizeAndRepaint`。
- **监听回调**：`columnAdded/columnRemoved/columnMoved/columnMarginChanged/columnSelectionChanged`。

## 2. Java 用户如何使用
- 构造：`JTableHeader h = new JTableHeader(columnModel);`
- 每个方法典型调用：`h.setReorderingAllowed(false)`；`h.setResizingAllowed(true)`；`h.setColumnModel(cm)`；`h.setTable(table)`；`h.columnAtPoint(new Point(x,y))`；`h.getHeaderRect(0)`；`h.updateUI()`；`h.getUIClassID()`。
- 真实组合链路：`new JTableHeader(cm) → setColumnModel → getColumnModel → columnAtPoint → getHeaderRect`。

## 3. 开源代码实际用法调研
1. `test/jdk/javax/swing/JTableHeader/bug4243927.java`：`JTableHeader header = new JTableHeader(); header.getColumnModel().addColumn(new TableColumn(0));`（本测试构造 + addColumn 同款用法，且该 bug 即为 AccessibleJTableHeader NPE）。
2. `test/jdk/javax/swing/JTableHeader/TableHeaderRendererTest.java`、`TableHeaderBorderPositionTest.java`、`SerializeJTableHeader.java` 覆盖渲染、边框、序列化。
3. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/JTableHeader.java`：构造器 `setColumnModel(cm)` → `initializeLocalVars()` → `updateUI()`；`getUIClassID()` 返回 `"TableHeaderUI"`。

## 4. 测试用例设计 + 自动化实现
落入 `JTableHeaderTest.java` 的 `testBlackBox()`：
- 默认/带模型构造；`set/getColumnModel` 往返；`set/getReorderingAllowed`、`set/getResizingAllowed`。
- 初始 `draggedColumn/resizingColumn==null`、`draggedDistance==0`；`set/getUpdateTableInRealTime`。
- 默认渲染器非空且可 `set/getDefaultRenderer`；`columnAtPoint`（负 x→-1，0→0）、`getHeaderRect(0)` 宽度、`getPreferredSize`。
- `getUIClassID()=="TableHeaderUI"`、`getUI`/`updateUI`、`getAccessibleContext`、`getToolTipText`（有效事件且无列→null）、各监听回调与 `resizeAndRepaint` 可调用。
共 39 个断言。
