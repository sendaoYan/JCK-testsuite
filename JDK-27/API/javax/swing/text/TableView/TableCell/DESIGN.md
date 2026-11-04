# javax.swing.text.TableView.TableCell — DESIGN

## 1. API 功能
表格单元格视图（内部 public 类，`extends BoxView`，Y_AXIS，implements `TableView.GridCell`）。核心方法：
- 构造：`TableCell(Element)`。
- 网格跨度：`getColumnCount()/getRowCount()`（默认 1）。
- 网格位置：`setGridLocation(int,int)/getGridRow()/getGridColumn()`。

## 2. Java 用户如何使用
由 TableView 内部创建（历史 `createTableCell`，现由 ViewFactory 生成）；测试通过 `tableView.new TableCell(elem)` 构造并验证默认单格跨度与网格位置往返。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TableView.java:551`：`class TableCell extends BoxView implements GridCell`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TableView.java:565`：`getColumnCount()` 返回 1。

## 4. 测试用例设计（黑盒部分，`TableCellTest.testBlackBox`）
1. 构造：`getAxis==Y_AXIS`、offsets。
2. 网格跨度：`getColumnCount()==1`、`getRowCount()==1`。
3. 网格位置：默认 (0,0)、`setGridLocation(2,3)` 后 `getGridRow==2/getGridColumn==3`。
4. 空单元格：`getPreferredSpan` X/Y 均为 0。
