# javax.swing.tree.TreeSelectionModel 行为化测试设计

## 1. API 功能
树选择模型接口，管理选中路径集合与行号映射。常量 `SINGLE_TREE_SELECTION=1`、`CONTIGUOUS_TREE_SELECTION=2`、`DISCONTIGUOUS_TREE_SELECTION=4`。
- 模式：`setSelectionMode/getSelectionMode`。
- 选择操作：`setSelectionPath/setSelectionPaths/addSelectionPath/addSelectionPaths/removeSelectionPath/removeSelectionPaths/clearSelection`。
- 查询：`getSelectionPath/getSelectionPaths/getSelectionCount/isPathSelected/isSelectionEmpty`。
- 行映射：`setRowMapper/getRowMapper/getSelectionRows/getMinSelectionRow/getMaxSelectionRow/isRowSelected/resetRowSelection/getLeadSelectionRow/getLeadSelectionPath`。
- 监听：`add/removePropertyChangeListener`、`add/removeTreeSelectionListener`。

## 2. Java 用户如何使用
匿名实现后用 `setSelectionMode(CONTIGUOUS)`、`setRowMapper(mapper)`、`setSelectionPaths(paths)`，再查询 `getSelectionRows/getMin/MaxSelectionRow/isRowSelected`；`DefaultTreeSelectionModel` 已实现该接口。

## 3. 开源代码实际用法调研
- JDK 源码 `DefaultTreeSelectionModel.java`：`getSelectionRows()` 返回 `getRowMapper().getRowsForPaths(getSelectionPaths())`；`setSelectionPaths` 触发 `TreeSelectionEvent` 与 `PropertyChangeEvent`。
- `test/jdk/javax/swing/tree/DefaultTreeSelectionModel/bug4485322.java`：`setSelectionPaths` + `setRowMapper` + `CONTIGUOUS_TREE_SELECTION` 后断言 `getSelectionPaths` 顺序。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `TreeSelectionModelTest.java`：
- 匿名实现维护 `ArrayList<TreePath>` 选择集与 `RowMapper`，行号由 mapper 映射 p0=0/p1=2/p2=5。
- 断言三个常量值、`setRowMapper/getRowMapper`、`setSelectionMode/getSelectionMode`。
- `setSelectionPaths` 后断言 count/paths/lead 与 `getSelectionRows/getMin/getMax/isRowSelected/getLeadSelectionRow`，`resetRowSelection` 不抛。
- `addSelectionPaths` 去重、`removeSelectionPath`、两类监听器往返、`clearSelection` 清空并返回 null 首路径；`DefaultTreeSelectionModel` 实现该接口。
