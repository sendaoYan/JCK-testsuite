# javax.swing.tree.DefaultMutableTreeNode 测试设计

## 1. API 功能
通用可变树节点：children Vector、userObject、allowsChildren、parent 引用；
insert/remove/add/removeFromParent/removeAllChildren、父子与兄弟查询、
四种遍历枚举（preorder/postorder/breadthFirst/depthFirst）与 getPath/getUserObjectPath/level/depth。

## 2. Java 用户如何使用
`DefaultMutableTreeNode root = new DefaultMutableTreeNode("root"); root.add(child);`
`root.preorderEnumeration()` 遍历；`node.getPath()` 给 TreePath 构造；`node.isNodeDescendant(x)` 判定关系。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/DefaultMutableTreeNode.java`；
`test/jdk/javax/swing/JTree/` 与 `test/jdk/javax/swing/tree/`。

## 4. 测试用例设计 + 易错细节
- 构造器默认：无 userObject、allowsChildren=true、无父子、children 枚举 EMPTY_ENUMERATION。
- add/insert/remove 语义与顺序；`insert` 越界 ArrayIndexOutOfBoundsException；
  `insert(node, -1)` 与索引超界抛 ArrayIndexOutOfBoundsException；insert 自身/祖先成环抛 IAE；
  remove(null)/remove(非子节点) 抛 IAE（"not a child"）；setAllowsChildren(false) 清空并拒绝后续 insert
  （IllegalStateException："node does not allow children"）。
- 方向易错：`x.isNodeAncestor(a)` 表示 a 是 x 的祖先（主语是被判定后代方）；
  root.getPreviousNode()==null、root.getNextNode()==首子、末节点 getNextNode()==null。
- 遍历：preorder "root,c1,g1,c2,"；depthFirstEnumeration 与 postorderEnumeration 顺序一致；
  breadthFirst 按层；耗尽后 next() 抛 NoSuchElementException。
- clone 丢 children/parent 但保留 userObject；serialization round-trip 恢复 parent 链且仍可变。
- null userObject 的 toString() 返回空串 ""（非 "null"）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
