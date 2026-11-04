# javax.swing.tree.RowMapper 行为化测试设计

## 1. API 功能
把 `TreePath[]` 映射为对应的 `int[]` 行号数组（未知路径约定返回 -1）。唯一方法：`int[] getRowsForPaths(TreePath[] path)`。

## 2. Java 用户如何使用
匿名实现：`new RowMapper() { public int[] getRowsForPaths(TreePath[] paths) { ... } }`；`AbstractLayoutCache`、`FixedHeightLayoutCache`、`VariableHeightLayoutCache` 均实现该接口，选择模型通过它把路径转行号。

## 3. 开源代码实际用法调研
- JDK 源码 `DefaultTreeSelectionModel#getSelectionRows()`：`return getRowMapper().getRowsForPaths(getSelectionPaths())`。
- JDK 源码 `AbstractLayoutCache#getRowsForPaths(TreePath[] path)`：实现 `RowMapper`，对每个 `TreePath` 调 `getRowForPath`，不可见路径返回 -1。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `RowMapperTest.java`：
- 匿名实现把 pA 映射为 3、pB 映射为 7、其余为 -1。
- 断言 `getRowsForPaths` 返回正确长度与各元素；`getRowsForPaths(null)` 返回 null。
- `AbstractLayoutCache` 与 `FixedHeightLayoutCache` 实现该接口。
