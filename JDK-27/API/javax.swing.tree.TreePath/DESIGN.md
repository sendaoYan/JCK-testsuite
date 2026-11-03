# javax.swing.tree.TreePath 行为化测试设计

## 1. API 功能
表示树中一条从根到节点的路径（不可变，元素数组 + 逐级 parent 链），实现 `Serializable`。
- 构造器：`TreePath(Object)`（单节点根路径）、`TreePath(Object[])`（多段路径，防御性）。
- 查询：`getPathCount`、`getPathComponent(int)`、`getPath()`（返回新数组）、`getLastPathComponent`、`getParentPath`。
- 派生：`pathByAddingChild(Object)`（不改变原路径）、`isDescendant(TreePath)`。
- 工具：`equals`、`hashCode`（取最后元素 hash）、`toString`。

## 2. Java 用户如何使用
构造与多步链路：`new TreePath(new Object[]{root, child, leaf})` → `getPathCount()` → `getLastPathComponent()` → `getParentPath()` → `pathByAddingChild(grand)` → `isDescendant(parent)` → `equals`/`hashCode` 比较。
- 每个方法典型调用：`p.getPathComponent(1)`；越界索引抛 `IllegalArgumentException`；`p.getPath()` 返回副本；`p.pathByAddingChild(x)` 返回长度 +1 的新路径而 `p` 不变；`child.isDescendant(p)` 判断 `p` 是否在 `child` 之下。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/JTree/bug4210432.java`：`Object[] tpa = {root, expansible, subexpansible1};` 再 `t.setSelectionPath(new TreePath(tpa));` 及 `t.isExpanded(new TreePath(tpa2))`。
- `test/jdk/javax/swing/JTree/DnD/LastNodeLowerHalfDrop.java`：`new TreePath(node.getPath())` 用 `TreeNode.getPath()` 构造 TreePath 后 `tree.getRowForPath(...)`。
- `test/jdk/javax/swing/tree/DefaultTreeSelectionModel/bug4485322.java`：`TreePath sPaths[] = ...; for (...) sPaths[i] = new TreePath(obj1[i]);` 构造选择路径数组。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `TreePathTest.java`：
- 单节点构造器与 `Object[]` 构造器的计数/组件/末组件断言。
- 验证 `Object[]` 构造与 `getPath()` 均防御性（构造后改输入数组、改返回数组不影响路径）。
- `getParentPath`、`pathByAddingChild`（断言不改变原路径）、`isDescendant`（正反 + 自身 + 无关）。
- `equals`/`hashCode` 往返、`toString` 含末组件、`getPathComponent(3)` 抛 `IllegalArgumentException`。
