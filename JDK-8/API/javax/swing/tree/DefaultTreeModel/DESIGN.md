# javax.swing.tree.DefaultTreeModel 测试设计

## 1. API 功能
TreeModel 默认实现：root、asksAllowsChildren（三态 isLeaf 判定）、insertNodeInto/removeNodeFromParent、
nodesWereInserted/nodesWereRemoved/nodesChanged/nodeChanged/nodeStructureChanged/reload 事件分发、
valueForPathChanged、getPathToRoot、getChild/getChildCount/getIndexOfChild。

## 2. Java 用户如何使用
`DefaultTreeModel m = new DefaultTreeModel(root); tree.setModel(m); m.insertNodeInto(node, parent, 0);`
UI 依 TreeModelEvent 增量刷新；`m.reload()` 全树重建。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/DefaultTreeModel.java`；
`test/jdk/javax/swing/tree/DefaultTreeSelectionModel/`、JTree 测试。

## 4. 测试用例设计 + 易错细节
- 事件载荷精确断言（Recorder）：insertNodeInto → INSERTED path=[父] children=[新] idx=[i]；
  removeNodeFromParent → REMOVED 携带 children 与 idx；nodeChanged(非根) → CHANGED path=[父] idx=[i]；
  nodeChanged(root) → CHANGED children=null/idx=null（源码 `else if (node == getRoot()) nodesChanged(node, null)`）；
  reload()/nodeStructureChanged/setRoot → STRUCT 全结构事件。
- asksAllowsChildren=false 时 isLeaf==getChildCount==0；=true 时 isLeaf 由 getAllowsChildren 决定。
- valueForPathChanged 回调 userObject；getIndexOfChild 未命中 -1；getChild 越界（字符串根喂给
  TreeNode 强转路径）抛 ClassCastException 的形态断言（用真实节点避开）。
- null/异常矩阵：getChildCount(null)/getChild(null,0)/isLeaf(null) 抛 NullPointerException；
  insertNodeInto(null child) 抛 IllegalArgumentException；removeNodeFromParent(root) 抛 IllegalArgumentException。
- 接口位置：implements TreeModel + Serializable；序列化 round-trip 后事件链与结构保留。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
