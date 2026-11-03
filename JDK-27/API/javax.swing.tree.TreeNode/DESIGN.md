# javax.swing.tree.TreeNode 行为化测试设计

## 1. API 功能
树节点只读接口：`getChildAt(int)`、`getChildCount()`、`getParent()`、`getIndex(TreeNode)`、`getAllowsChildren()`、`isLeaf()`、`children()`（返回 `Enumeration<? extends TreeNode>`）。

## 2. Java 用户如何使用
自定义节点实现全部方法后接入 `TreeModel`/`DefaultMutableTreeNode`；`DefaultMutableTreeNode` 已实现该接口，是 Swing 树的标准节点实现。

## 3. 开源代码实际用法调研
- JDK 源码 `DefaultMutableTreeNode.java`：实现 `TreeNode`，`children()` 返回 `new Enumeration<TreeNode>() { ... }`；`isLeaf()` 返回 `children == null || children.size() == 0`。
- JDK 源码 `JTree.java`：`convertValueToText` 取 `TreeNode` 的 `toString()` 作为显示文字。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `TreeNodeTest.java`：
- 匿名实现一个叶子（childCount 0、children 空枚举、isLeaf true）与一个父节点（含一个子节点）。
- 分别对父与子断言全部 7 个方法（含 `getIndex` 非子节点返回 -1、空枚举）。
- `DefaultMutableTreeNode` 实现该接口。
