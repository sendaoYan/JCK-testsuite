# javax.swing.tree.FixedHeightLayoutCache 行为化测试设计

## 1. API 功能
`AbstractLayoutCache` 的具体实现，把所有行按固定行高布局（`rowHeight > 0`）。
- 构造器：`FixedHeightLayoutCache()`（默认行高 1）。
- 配置：`setModel`、`setRootVisible`、`setRowHeight`（非正抛 `IllegalArgumentException`）、继承 `setNodeDimensions`。
- 布局查询：`getRowCount`、`isExpanded`、`getBounds`、`getPathForRow`、`getRowForPath`、`getPathClosestTo`、`getVisibleChildCount`、`getVisiblePathsFrom`、`setExpandedState`、`getExpandedState`。
- 工具：`invalidateSizes`、`invalidatePathBounds`、继承 `getPreferredHeight/getPreferredWidth/getRowsForPaths`。

## 2. Java 用户如何使用
构造与多步链路：`new FixedHeightLayoutCache()` → `setNodeDimensions(nd)` → `setRowHeight(5)` → `setRootVisible(true)` → `setModel(model)` → `getRowCount()` → `getRowForPath(path)` → `getBounds(path, null)` → `setExpandedState(path, true)` → `getPreferredHeight()`。
- 每个方法典型调用：`getPathForRow(0)`；`getRowForPath(不可见路径)` 返回 -1；`getBounds` 的 y 由 `row * rowHeight` 得出、height 固定为行高、width/x 由 NodeDimensions 提供；`getPathClosestTo` 越界夹到末行。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/tree/FixedHeightLayoutCache/bug4210354.java`：`new FixedHeightLayoutCache()` + `setModel(dim.getTreeModelILike(3))` + `setRootVisible(true)` + `setNodeDimensions(dim)` + `setRowHeight(h)`，再 `getPathForRow(row)` + `getBounds(path, new Rectangle())` 校验宽度。
- JDK 源码 `javax/swing/plaf/basic/BasicTreeUI.java#createLayoutCache()`：`if (isLargeModel() && getRowHeight() > 0) return new FixedHeightLayoutCache();`（大模型 + 固定行高时使用）。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `FixedHeightLayoutCacheTest.java`：
- 三层模型，匿名 `NodeDimensions` 使宽度随深度 `depth*10`、宽度 20；`setRowHeight(5)` + `setRootVisible(true)` + `setModel`。
- 断言根默认展开、`child1` 折叠、`getRowCount==3`、行映射、`getVisibleChildCount`、不可见 `grand` 行 -1。
- `getBounds`（x/width/height/y 精确值）、`getPreferredHeight==15`、`getRowsForPaths`、`getPathClosestTo`。
- `setExpandedState` 展开/折叠后行数、`getPreferredWidth==40`、可见路径枚举顺序；`invalidateSizes/invalidatePathBounds` 不抛；`setRowHeight(0)` 抛 `IllegalArgumentException`。
