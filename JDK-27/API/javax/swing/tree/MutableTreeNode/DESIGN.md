# javax.swing.tree.MutableTreeNode 行为化测试设计

## 1. API 功能
`TreeNode` 的变异子接口，为可变树节点定义增删改接口。
- 新增/删除：`insert(MutableTreeNode,int)`、`remove(int)`、`remove(MutableTreeNode)`。
- 修改：`setUserObject(Object)`、`setParent(MutableTreeNode)`。
- 移除自身：`removeFromParent()`（从父节点移除自身并清除父引用）。

## 2. Java 用户如何使用
自定义可变节点实现全部方法后：`parent.insert(child, 0)` → `child.setParent(parent)` → `parent.setUserObject("x")` → `child.removeFromParent()`；或直接使用 `DefaultMutableTreeNode`（已实现该接口）。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/JTree/NodeChangedTest.java`：`root.add(new DefaultMutableTreeNode(...))` 通过 `MutableTreeNode.insert/remove` 增删子节点。
- JDK 源码 `DefaultMutableTreeNode.java`：`insert(MutableTreeNode,int)` 先 `remove` 旧父再设置新父并 `fireTreeNodesInserted`；`removeFromParent()` 调 `parent.remove(this)`。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `MutableTreeNodeTest.java`：
- 匿名实现全部方法，用 `ArrayList` 保存子节点、数组引用保存父与 userObject。
- 断言 `insert/getChildCount/getChildAt/getIndex/children/isLeaf` 组合；`setUserObject` 存值。
- `setParent(parent)/getParent`、`setParent(null)`、`add` 反向设置父、`removeFromParent` 从父移除并清父引用。
- `remove(int)`/`remove(MutableTreeNode)` 后回到叶子；`DefaultMutableTreeNode` 实现该接口。
