# javax.swing.tree.DefaultTreeCellRenderer 行为化测试设计

## 1. API 功能
`JTree` 默认的 `TreeCellRenderer`，继承 `JLabel`，负责把节点渲染成带图标/文字/前景背景色的标签。
- 构造器：`DefaultTreeCellRenderer()`（`updateUI` 装载 L&F 默认图标/颜色）。
- 图标：`getDefaultOpenIcon/getDefaultClosedIcon/getDefaultLeafIcon`，`setOpenIcon/getOpenIcon`、`setClosedIcon/getClosedIcon`、`setLeafIcon/getLeafIcon`。
- 颜色：`setTextSelectionColor/getTextSelectionColor`、`setTextNonSelectionColor/getTextNonSelectionColor`、`setBackgroundSelectionColor/getBackgroundSelectionColor`、`setBackgroundNonSelectionColor/getBackgroundNonSelectionColor`、`setBorderSelectionColor/getBorderSelectionColor`。
- 渲染：`getTreeCellRendererComponent(JTree,Object,selected,expanded,leaf,row,hasFocus)`（返回自身）、`updateUI`；继承 `setFont/getFont`、`setBackground` 等。

## 2. Java 用户如何使用
构造与多步链路：`new DefaultTreeCellRenderer()` → `setLeafIcon(icon)` → `setBackgroundNonSelectionColor(color)` → `getTreeCellRendererComponent(new JTree(), value, true, true, false, 0, true)` 得到组件并读取 `getText()`/图标。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicTreeUI.java`：`currentCellRenderer.getTreeCellRendererComponent(tree, path.getLastPathComponent(), tree.isRowSelected(row), isExpanded, isLeaf, row, (leadIndex == row))`，再用 `rendererPane.paintComponent(...)` 绘制。
- JDK 源码 `BasicTreeUI#createDefaultCellRenderer()`：`return new DefaultTreeCellRenderer();` 作为 JTree 默认渲染器。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `DefaultTreeCellRendererTest.java`：
- 断言三个默认图标非空（Metal L&F 无头可用）。
- 自定义 `Icon` 的 `set/getLeafIcon/OpenIcon/ClosedIcon` 往返；五种颜色的 set/get 往返；`Font` 往返。
- `getTreeCellRendererComponent` 返回自身、为 `JLabel`、文字等于传入 value（headless-safe `new JTree()`）。
