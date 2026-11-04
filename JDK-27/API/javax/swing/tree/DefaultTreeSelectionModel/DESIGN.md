# javax.swing.tree.DefaultTreeSelectionModel 行为化测试设计

## 1. API 功能
`TreeSelectionModel` 的默认实现，维护一组选中 `TreePath`，支持单选/连续/不连续三种模式与 `RowMapper` 行映射。
- 构造器：`DefaultTreeSelectionModel()`（默认 `DISCONTIGUOUS_TREE_SELECTION`）。
- 选择操作：`setSelectionPath(s)`、`addSelectionPath(s)`、`removeSelectionPath(s)`、`clearSelection`、`resetRowSelection`。
- 查询：`getSelectionPath(s)`、`getSelectionCount`、`isPathSelected`、`isSelectionEmpty`、`getLeadSelectionPath`。
- 模式/映射：`setSelectionMode`、`getSelectionMode`、`setRowMapper`、`getRowMapper`、`getSelectionRows`、`getMin/MaxSelectionRow`、`isRowSelected`、`getLeadSelectionRow`。
- 监听：`addTreeSelectionListener`、`removeTreeSelectionListener`、`getTreeSelectionListeners`；`clone`。

## 2. Java 用户如何使用
构造与多步链路：`new DefaultTreeSelectionModel()` → `setRowMapper(mapper)` → `setSelectionPath(p0)` → `addSelectionPath(p1)` → `getSelectionCount()` → `getSelectionRows()` → `isPathSelected(p1)` → `removeSelectionPath(p1)` → `clearSelection()`。
- 每个方法典型调用：`setSelectionMode(SINGLE_TREE_SELECTION)` 只保留 `setSelectionPaths` 传入的第一条；`CONTIGUOUS` 模式下不连续路径只保留第一条；`addTreeSelectionListener(l)` 后 `addSelectionPath` 触发 `valueChanged`，`isAddedPath()` 区分增删。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/tree/DefaultTreeSelectionModel/bug4485322.java`：`model.setSelectionPaths(sPaths)` + `model.setRowMapper(new DummyRowMapper())` + `model.setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION)` + `model.insureRowContinuity()` 后校验 `getSelectionPaths()`。
- JDK 源码 `DefaultTreeSelectionModel.getSelectionRows()` 调用 `rowMapper.getRowsForPaths(selection)`；`resetRowSelection()` 亦按 `rowMapper` 重建行区间。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `DefaultTreeSelectionModelTest.java`：
- 匿名 `RowMapper` 将 `p0/p1/p2` 映射到行 `0/2/5`；验证默认模式、初始为空。
- `setSelectionPath` + 连续 `addSelectionPath` 断言顺序 `[p0,p1,p2]`、行 `[0,2,5]`、min/max 行、`isRowSelected`、lead path/row（在 remove 前确定）。
- `removeSelectionPath` 后只断言成员与计数（实现用 Hashtable 重建顺序不确定，故不排序断言顺序）。
- 监听器增删与 `isAddedPath` 增删事件；三种 `setSelectionMode` 下 `setSelectionPaths` 的首条保留语义；`setSelectionPath(null)`/`clearSelection` 清空；`resetRowSelection` 保活；`clone` 独立复制选择。
