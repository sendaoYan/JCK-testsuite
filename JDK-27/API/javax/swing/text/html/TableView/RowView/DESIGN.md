# javax.swing.text.html.TableView.RowView — DESIGN

## 1. API 功能
HTML 表格行视图（`public class RowView extends BoxView`），代表 `<tr>`，沿 X 轴水平排列单元格视图。关键方法：
- `getResizeWeight(int axis)`：X/Y 轴均返回 1（高度/宽度随表格伸缩）。
- `getAttributes()`：返回行元素的属性。
- `getMinimumSpan/getPreferredSpan/getMaximumSpan`：行跨度的最小/首选/最大度量。

## 2. Java 用户如何使用
由 `TableView` 内部为每个 `<tr>` 创建 RowView。测试可通过编辑器视图树递归收集所有 RowView 实例（两个 `<tr>` 对应两个 RowView），再查询伸缩权重与跨度。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/TableView.java`：内部 `RowView` 继承 BoxView，`getResizeWeight` 双轴 1。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/TableView/bug4412522.java`：表格行布局回归。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/CSS/bug4174871.java`：表格 CSS 布局回归。

## 4. 测试用例设计（黑盒部分，`RowViewTest.testBlackBox`）
1. 收集视图树中全部 RowView，两个 `<tr>` 得到 2 个实例。
2. `getResizeWeight(X_AXIS) == 1`、`getResizeWeight(Y_AXIS) == 1`。
3. `getStartOffset <= getEndOffset`；`getAttributes()` 非 null 且稳定。
4. 各轴 span 非负且 `min <= pref <= max`。
