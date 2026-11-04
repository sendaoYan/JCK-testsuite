# TableColumn 测试设计

## 1. API 功能
`javax.swing.table.TableColumn` 表示表格中的一列，承载视图列到模型列的映射与显示属性，实现 `Serializable`。核心分组：

- **构造器**：`()`、`(int modelIndex)`、`(int modelIndex, int width)`、`(int modelIndex, int width, TableCellRenderer, TableCellEditor)`。
- **标识/取值**：`set/getModelIndex`、`set/getIdentifier`、`set/getHeaderValue`。
- **宽度**：`set/getPreferredWidth`、`set/getMinWidth`、`set/getMaxWidth`、`set/getWidth`（自动夹取到 [min,max]）、`sizeWidthToFit`。
- **渲染/编辑**：`set/getHeaderRenderer`、`set/getCellRenderer`、`set/getCellEditor`。
- **其他**：`set/getResizable`、`disableResizedPosting`/`enableResizedPosting`、`add/remove/getPropertyChangeListeners`。

## 2. Java 用户如何使用
- 构造：`TableColumn c = new TableColumn(0, 100);`
- 每个方法典型调用：`c.setHeaderValue("Name")`；`c.setIdentifier("col0")`；`c.setPreferredWidth(120)`；`c.setMinWidth(20)`；`c.setMaxWidth(300)`；`c.setCellRenderer(renderer)`；`c.setCellEditor(editor)`；`c.setResizable(false)`；`c.getWidth()`。
- 真实组合链路：`new TableColumn(modelIndex) → setIdentifier → setHeaderValue → setPreferredWidth/setMinWidth/setMaxWidth → setCellRenderer → setWidth（自动夹取）→ getWidth/getPreferredWidth`。

## 3. 开源代码实际用法调研
1. SwingSet3 `test/jdk/sanity/client/lib/SwingSet3/src/com/sun/swingset3/demos/table/TableDemo.java`：逐个 `new TableColumn()` 后 `setModelIndex(...)`、`setHeaderValue(...)`、`setPreferredWidth(...)`、`setCellRenderer(...)`，再 `columnModel.addColumn(column)`（典型建列链路）。
2. `test/jdk/javax/swing/JTableHeader/bug4243927.java`：`header.getColumnModel().addColumn(new TableColumn(0))` 直接构造列并加入列模型。
3. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/TableColumn.java`：`setWidth` 用 `Math.min(Math.max(width, minWidth), maxWidth)` 夹取后再 `firePropertyChange`，是宽度夹取行为的权威依据。

## 4. 测试用例设计 + 自动化实现
落入 `TableColumnTest.java` 的 `testBlackBox()`：
- 默认构造器默认值（modelIndex 0、width/preferredWidth 75、minWidth 15、maxWidth Integer.MAX_VALUE、resizable true、headerValue/identifier null）。
- `set/get` 全属性往返；宽度夹取（setWidth 超出 max 收敛到 max、低于 min 收敛到 min）。
- 三个渲染/编辑属性与 4 参构造器往返。
- `PropertyChangeListener`：setIdentifier/setHeaderValue/setModelIndex/setWidth 触发 property 变化（断言属性名与 old/new 值），移除后不再触发。
- `sizeWidthToFit` 后 `getWidth()==getPreferredWidth()`、`minWidth>=0`。
共 46 个断言。
