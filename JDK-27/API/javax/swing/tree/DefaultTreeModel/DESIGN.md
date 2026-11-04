# javax.swing.tree.DefaultTreeModel 行为化测试设计

## 1. API 功能
`TreeModel` 的默认实现，基于 `TreeNode` 并提供事件派发（`TreeModelListener`）。
- 构造器：`DefaultTreeModel(TreeNode)`、`DefaultTreeModel(TreeNode, boolean asksAllowsChildren)`。
- 模型查询：`getRoot`、`setRoot`、`getChild`、`getChildCount`、`getIndexOfChild`、`isLeaf`、`getPathToRoot`。
- 变更通知：`insertNodeInto`、`removeNodeFromParent`、`nodeChanged`、`nodeStructureChanged`、`reload`、`reload(node)`、`valueForPathChanged`、以及底层 `nodesWereInserted/nodesWereRemoved/nodesChanged`。
- 监听：`addTreeModelListener`、`removeTreeModelListener`、`getTreeModelListeners`、`getListeners`。

## 2. Java 用户如何使用
构造与多步链路：`new DefaultTreeModel(root)` → `getRoot()` → `getChildCount(root)` → `getIndexOfChild(root, child)` → `insertNodeInto(newChild, root, 1)`（触发 `treeNodesInserted`）→ `nodeChanged(child)`（触发 `treeNodesChanged`）→ `removeNodeFromParent(newChild)`（触发 `treeNodesRemoved`）→ `nodeStructureChanged(root)` / `reload()`（触发 `treeStructureChanged`）→ `valueForPathChanged(path, newValue)`。
- 每个方法典型调用：`model.getChild(root, 0)`；`model.isLeaf(leafNode)` 取决于 `asksAllowsChildren`；`model.getPathToRoot(node)` 返回从根到节点数组；`model.addTreeModelListener(l)` 后用 `getTreeModelListeners()` 验证。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/JTree/NodeChangedTest.java`：`new DefaultTreeModel(root)` 作 `JTree` 模型，`root.setUserObject(...)` 后 `((DefaultTreeModel)tree.getModel()).nodeChanged(root)` 刷新。
- `test/jdk/javax/swing/JTree/bug4210432.java`：`DefaultTreeModel model = new DefaultTreeModel(root); JTree t = new JTree(model);`。
- `test/jdk/javax/swing/JTree/DnD/LastNodeLowerHalfDrop.java`：`new DefaultTreeModel(root)` 作为拖拽测试树模型。
- JDK 源码 `JTree` 大量使用 `DefaultTreeModel` 作为默认模型并调用 `reload/nodeChanged` 等。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `DefaultTreeModelTest.java`：
- 构造器与 `asksAllowsChildren` 往返；`getRoot/setRoot/getChild/getChildCount/getIndexOfChild/isLeaf/getPathToRoot` 断言（含 null 参数、非子节点 -1）。
- 注册 `TreeModelListener` 记录器，依次执行 `insertNodeInto`（断言 `treeNodesInserted` 的 `getChildIndices()==[1]` 与 `getChildren()`）、`nodeChanged`（`[0]`）、`valueForPathChanged`（改 userObject 并再触发 `treeNodesChanged`）、`removeNodeFromParent`（`[1]` + removed children）、`nodeStructureChanged` 与 `reload`（`treeStructureChanged`，childIndices 为 null）。
- `getTreeModelListeners`/`removeTreeModelListener` 往返；`setRoot` 换根；`new DefaultTreeModel(null)` 容忍空根。
