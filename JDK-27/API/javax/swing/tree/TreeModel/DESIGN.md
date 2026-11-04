# javax.swing.tree.TreeModel 行为化测试设计

## 1. API 功能
`JTree` 的数据模型接口，提供树的读接口与变更通知。
- 结构：`getRoot()`、`getChild(parent,index)`、`getChildCount(parent)`、`isLeaf(node)`、`getIndexOfChild(parent,child)`。
- 修改：`valueForPathChanged(TreePath,Object)`。
- 监听：`addTreeModelListener`、`removeTreeModelListener`。

## 2. Java 用户如何使用
匿名实现后交给 `JTree`：`getChild/getChildCount/getIndexOfChild/isLeaf` 基于 `TreeNode`，`valueForPathChanged` 更新节点 userObject 并触发 `TreeModelEvent`；`DefaultTreeModel` 已实现该接口。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/JTree/NodeChangedTest.java`：`DefaultTreeModel` 包装 `DefaultMutableTreeNode`，`addTreeModelListener` 监听 `treeNodesChanged` 后 `model.nodeChanged` 触发。
- JDK 源码 `DefaultTreeModel.java`：`valueForPathChanged` 调 `node.setUserObject(newValue)` 并 `fireTreeNodesChanged`，`getIndexOfChild` 对非子节点返回 -1。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `TreeModelTest.java`：
- 匿名实现包装根 + 两个叶子的 `DefaultMutableTreeNode`。
- 断言 `getRoot`、`getChildCount`、`getChild`、`isLeaf`、`getIndexOfChild`（含非子节点 -1）。
- `add/removeTreeModelListener` 往返；`valueForPathChanged` 置位并更新 userObject；`DefaultTreeModel` 实现该接口。
