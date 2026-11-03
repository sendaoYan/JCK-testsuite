# javax.swing.tree.VariableHeightLayoutCache 行为化测试设计

## 1. API 功能
`AbstractLayoutCache` 的具体实现，行高由 `NodeDimensions` 决定（可变行高），是 `JTree` 非大模型/非固定行高时的默认布局缓存。
- 构造器：`VariableHeightLayoutCache()`（`rowHeight<=0` 为可变模式）。
- 配置：`setModel`、`setRootVisible`、`setRowHeight`、`setNodeDimensions`。
- 布局查询：`setExpandedState`、`getExpandedState`、`getBounds`、`getPathForRow`、`getRowForPath`、`getRowCount`、`getPathClosestTo`、`getVisiblePathsFrom`、`getVisibleChildCount`、`isExpanded`。
- 尺寸：覆盖 `getPreferredHeight`、`getPreferredWidth`；`invalidateSizes`、`invalidatePathBounds`。

## 2. Java 用户如何使用
构造与多步链路：`new VariableHeightLayoutCache()` → `setNodeDimensions(nd)` → `setRootVisible(true)` → `setModel(model)` → `getRowCount()` → `getBounds(path,null)` → `getPreferredHeight()` → `setExpandedState(childPath,true)` → `getPreferredWidth(null)`。
- 每个方法典型调用：`getRowForPath(隐藏路径)` 返回 -1；`getBounds` 的 x/width/height 全部来自 NodeDimensions、y 为各可见节点高度累加；`getPreferredHeight()` 取末可见节点 `yOrigin + height`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicTreeUI.java#createLayoutCache()`：`if (isLargeModel() && getRowHeight() > 0) return new FixedHeightLayoutCache(); return new VariableHeightLayoutCache();` —— 可变行高是 JTree 默认布局缓存。
- JDK 源码 `VariableHeightLayoutCache.java` 内部 `getMaxNodeWidth()` 遍历可见节点取 `getPreferredWidth() + getXOrigin()` 的最大值，供 `getPreferredWidth` 返回。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `VariableHeightLayoutCacheTest.java`：
- 三层模型 + 匿名 `NodeDimensions`（width 20、height 15、x=`depth*10`），`setNodeDimensions`/`setRootVisible(true)`/`setModel`。
- 断言可变模式 `getRowHeight()==0`、根默认展开、行数 3、行映射、`getBounds` 的 x/y/width/height、`getPreferredHeight==45`、`getPreferredWidth==30`。
- 展开 `child1` 后行数 4、`grand` 行 3、`getPreferredHeight==60`、`getPreferredWidth==40`；折叠恢复。
- `getRowsForPaths`、`getPathClosestTo`、可见路径枚举、`invalidateSizes/invalidatePathBounds`、`setRowHeight(20)/setRowHeight(0)` 往返。
