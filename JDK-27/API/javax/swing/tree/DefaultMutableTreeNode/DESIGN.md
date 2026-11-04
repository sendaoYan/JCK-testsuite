# javax.swing.tree.DefaultMutableTreeNode 行为化测试设计

## 1. API 功能
通用的可变树节点（同时实现 `MutableTreeNode` + `TreeNode`），是 `JTree` 默认模型（`DefaultTreeModel`）的标准节点。
- 构造器：`DefaultMutableTreeNode()`、`DefaultMutableTreeNode(Object)`、`DefaultMutableTreeNode(Object, boolean allowsChildren)`。
- 结构维护（MutableTreeNode）：`add`、`insert(child,index)`、`remove(int)`、`remove(MutableTreeNode)`、`removeFromParent`、`removeAllChildren`、`setParent`、`setUserObject`、`setAllowsChildren`。
- 查询：`getParent`、`getChildAt`、`getChildCount`、`getIndex`、`getUserObject`、`getAllowsChildren`、`children`、`getFirstChild/getLastChild`、`getChildAfter/getChildBefore`。
- 层级/关系：`getLevel`、`getDepth`、`getPath`、`getUserObjectPath`、`getRoot`、`isRoot`、`isNodeAncestor/isNodeDescendant/isNodeChild/isNodeSibling/isNodeRelated`、`getSharedAncestor`、`getSiblingCount`、`getNextSibling/getPreviousSibling`。
- 遍历：`preorderEnumeration`、`postorderEnumeration`、`breadthFirstEnumeration`、`depthFirstEnumeration`、`pathFromAncestorEnumeration`；叶子遍历 `getFirstLeaf/getLastLeaf/getNextLeaf/getPreviousLeaf/getLeafCount`。
- 工具：`isLeaf`、`toString`、`clone`（浅拷贝，无父子）、静态 `EMPTY_ENUMERATION`。

## 2. Java 用户如何使用
构造与多步链路：`new DefaultMutableTreeNode("root")` → `add(child)` / `insert(child, index)` → `getChildAt(i)` → `getIndex(node)` → `getLevel()/getDepth()` → `getPath()` → `getSharedAncestor(other)` → `preorderEnumeration()` 收集后遍历 → `removeFromParent()` / `removeAllChildren()`。
- 每个方法典型调用：`root.add(new DefaultMutableTreeNode("a", false))`；`root.insert(newChild, 1)` 会先从旧父节点摘下再插入；`root.getChildAt(0)`；`root.getIndex(child)` 非子节点返回 -1；`a1.getPath()` 返回 `[root,a,a1]`；`root.isNodeAncestor(a1)`；`a1.getSharedAncestor(a2)` 返回最近公共祖先；`root.preorderEnumeration()` 先根后子；`node.clone()` 得到无父子、共享 userObject 的浅拷贝。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/JTree/NodeChangedTest.java`：`new DefaultMutableTreeNode("root", true)` + `root.add(child)` + `child.add(leaf)`，再 `root.setUserObject("New root")` + `((DefaultTreeModel)tree.getModel()).nodeChanged(root)`。
- `test/jdk/javax/swing/JTree/bug4210432.java`：多级 `DefaultMutableTreeNode` + `expansible.add(subexpansible1)`，配合 `new TreePath(tpa)` 选择不可见节点。
- `test/jdk/javax/swing/JTree/DnD/LastNodeLowerHalfDrop.java`：`Enumeration e = root.breadthFirstEnumeration();` 循环 `e.nextElement()` 展开整棵树。
- `test/jdk/performance/client/SwingMark/src/TreeTest.java`：`for (Enumeration e = node.depthFirstEnumeration(); e.hasMoreElements();)` 收集节点再删除。
- `test/jdk/java/awt/a11y/AccessibleJTreeTest.java`：`((DefaultMutableTreeNode) value).getUserObject()` 读取节点数据。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `DefaultMutableTreeNodeTest.java`：
- 三层结构 `root -> (a -> (a1,a2)), b(叶子), c`，用 `add`/`insert(b,1)` 构造并验证子节点顺序。
- 覆盖父子/索引查询、`getLevel/getDepth`、`getPath/getUserObjectPath`、`isLeaf`、祖先/后代/共享祖先/兄弟关系、`getNextSibling/getPreviousSibling`、`getNextNode/getPreviousNode`、首末叶与叶子计数。
- 四种遍历收集到 `List<String>`（用 userObject 的 `toString`）后与固定有序列表比较，保证确定性。
- `setUserObject`/`toString`/`clone` 往返；独立子树验证 `remove(int)`/`removeFromParent`/`remove(MutableTreeNode)`/`removeAllChildren`/`insert` 重新挂接、`setAllowsChildren(false)` 清空子节点。
- 断言 `EMPTY_ENUMERATION` 为空、构造器默认值。
