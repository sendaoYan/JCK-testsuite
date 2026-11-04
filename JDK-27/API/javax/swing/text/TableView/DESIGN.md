# javax.swing.text.TableView — DESIGN

## 1. API 功能
表格视图抽象类（`extends BoxView`，Y_AXIS）。核心方法：
- 构造：`TableView(Element)`（创建 rows 向量与 totalColumnRequirements）。
- 行列工厂：`createTableRow(Element)`（protected，返回 TableRow）、`createTableCell(Element)`（@Deprecated protected）。
- 网格：`getColumnsOccupied/getRowsOccupied/getColumnSpan/getRowSpan`（部分包私有）、`updateGrid/invalidateGrid`。
- 子视图：`replace(int,int,View[])`（覆盖后 invalidateGrid）。
- 内部类：`TableRow`（X_AXIS 行）、`TableCell`（Y_AXIS 单元格，implements GridCell）。

## 2. Java 用户如何使用
抽象类，由 `javax.swing.text.html.TableView` 子类化；`new TableView(elem)` 后 `replace(0,0,new View[]{row})` 加入 TableRow，`getViewCount/getView` 遍历行。行/列数由 `updateGrid` 按 colspan/rowspan 计算。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java:1389`：HTML 表格 `return new javax.swing.text.html.TableView(elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TableView.java:183`：`createTableRow` 返回 `new TableRow(elem)`。

## 4. 测试用例设计（黑盒部分，`TableViewTest.testBlackBox`）
1. 子类构造：`getAxis==Y_AXIS`、offsets。
2. `createTableRow`（子类暴露）返回 TableRow 且 `getAxis==X_AXIS`。
3. 未 replace 时 `getViewCount==0`；`replace(0,0,[row])` 后 `getViewCount==1`、`getView(0)==row`。
