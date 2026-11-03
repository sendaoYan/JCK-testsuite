# javax.swing.tree.AbstractLayoutCache 行为化测试设计

## 1. API 功能
抽象布局缓存，实现 `RowMapper`，是 `FixedHeightLayoutCache` 与 `VariableHeightLayoutCache` 的公共基类。
- 配置：`setNodeDimensions`、`getNodeDimensions`、`setModel`、`getModel`、`setRootVisible`、`isRootVisible`、`setRowHeight`、`getRowHeight`、`setSelectionModel`、`getSelectionModel`。
- 尺寸/映射（具体）：`getPreferredHeight`、`getPreferredWidth(Rectangle)`、`getRowsForPaths(TreePath[])`（RowMapper）。
- 抽象方法（子类实现）：`isExpanded`、`getBounds`、`getPathForRow`、`getRowForPath`、`getPathClosestTo`、`getVisiblePathsFrom`、`getVisibleChildCount`、`setExpandedState`、`getExpandedState`、`getRowCount`、`invalidateSizes`、`invalidatePathBounds`、四个 `treeNodes*` 回调。
- 内部抽象静态类 `NodeDimensions`：`getNodeDimensions(Object,int,int,boolean,Rectangle)`。

## 2. Java 用户如何使用
通过具体子类使用：`AbstractLayoutCache cache = new FixedHeightLayoutCache();` → `setNodeDimensions(nd)` → `setModel(model)` → `setRootVisible(true)` → `setRowHeight(5)` → `setSelectionModel(sm)` → `getRowsForPaths(paths)` → `getPreferredHeight()`/`getPreferredWidth(null)`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicTreeUI.java#createLayoutCache()` 返回 `AbstractLayoutCache`（固定/可变两种子类），UI 再通过其抽象方法 `getPathForRow/getRowForPath/getBounds` 绘制。
- JDK 源码 `DefaultTreeSelectionModel#setRowMapper(AbstractLayoutCache)`：`treeSelectionModel.setRowMapper(this)` 由 `AbstractLayoutCache.setSelectionModel` 调用，把缓存作为 RowMapper 注入选择模型。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `AbstractLayoutCacheTest.java`：
- 匿名具体子类实现 16 个抽象方法（固定返回单行、固定 `Rectangle(0,0,10,10)`、空枚举、no-op 回调）。
- 断言 `setNodeDimensions/getNodeDimensions`、`setModel/getModel`、`setRootVisible/isRootVisible`、`setRowHeight/getRowHeight`、`setSelectionModel/getSelectionModel` 往返。
- 通过 `getRowCount/getPathForRow/getRowForPath/getPathClosestTo/getBounds` 驱动抽象方法；`getPreferredHeight==10`、`getPreferredWidth(null)==10`；`getRowsForPaths` 映射与 null 分支；`setExpandedState/invalidateSizes/invalidatePathBounds` 不抛。
