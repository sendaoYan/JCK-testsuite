# javax.swing.text.TableView.TableRow — DESIGN

## 1. API 功能
表格行视图（内部 public 类，`extends BoxView`，X_AXIS）。核心方法：
- 构造：`TableRow(Element)`（BitSet fillColumns 记录跨列填充）。
- 尺寸：`getMinimumSpan/getPreferredSpan`（X 轴 = totalColumnRequirements + 内边距）、`getMaximumSpan(X_AXIS)==Integer.MAX_VALUE`；次轴继承 BoxView。
- `replace(int,int,View[])` 覆盖后 `invalidateGrid()`；列计数 `getColumnCount()`（含跨列填充，包私有）。

## 2. Java 用户如何使用
由 TableView 内部创建（`createTableRow`），行内放入 TableCell 子视图；测试通过 `tableView.new TableRow(elem)` 构造并验证空行 X 轴跨度 0、Y 次轴无界。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TableView.java:498`：`class TableRow extends BoxView`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TableView.java:545`：`getMaximumSpan(X_AXIS)` 返回 `Integer.MAX_VALUE`。

## 4. 测试用例设计（黑盒部分，`TableRowTest.testBlackBox`）
1. 构造：`getAxis==X_AXIS`、offsets。
2. 空行：X 轴 `getMinimumSpan/getPreferredSpan==0`、`getMaximumSpan==Integer.MAX_VALUE`。
3. 次轴 Y：minimum/preferred==0、maximum==Integer.MAX_VALUE（BoxView 次轴无界）。
