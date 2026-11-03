# javax.swing.tree.TreeCellRenderer 行为化测试设计

## 1. API 功能
树单元格渲染器接口，唯一方法：`Component getTreeCellRendererComponent(JTree,Object,selected,expanded,leaf,row,hasFocus)`，返回一个渲染组件（通常是带文字/图标的标签）。

## 2. Java 用户如何使用
匿名实现：`new TreeCellRenderer() { public Component getTreeCellRendererComponent(...) { label.setText(String.valueOf(value)); return label; } }`；`JTree` 用它绘制每个可见节点，`DefaultTreeCellRenderer` 已实现该接口。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicTreeUI.java`：`currentCellRenderer.getTreeCellRendererComponent(tree, path.getLastPathComponent(), tree.isRowSelected(row), isExpanded, isLeaf, row, (leadIndex == row))`，再用 `rendererPane.paintComponent(...)` 绘制。
- JDK 源码 `DefaultTreeCellRenderer.java`：`getTreeCellRendererComponent` 返回自身并设置图标、前景/背景色与文字。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `TreeCellRendererTest.java`：
- 匿名实现返回固定 `JLabel` 并写入 value 文字。
- 断言返回同一标签实例、标签文字等于传入 value；`DefaultTreeCellRenderer` 实现该接口。
