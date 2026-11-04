# javax.swing.tree.TreeNode 测试设计

## 1. API 功能
只读树节点契约：7 个方法（getChildAt/getChildCount/getParent/getIndex/getAllowsChildren/
isLeaf/children），与 MutableTreeNode 一起构成 Swing 树节点双接口。

## 2. Java 用户如何使用
自定义只读节点（如缓存目录树）实现 TreeNode；`node.children()` 返回 Enumeration 遍历子节点。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/TreeNode.java`；
JTree/DefaultTreeModel 的泛型调用点。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、7 个方法名（getIndex/getChildAt/getChildCount/getParent/isLeaf/
  getAllowsChildren/children）、全部 abstract。
- 已知实现：MutableTreeNode extends TreeNode；DefaultMutableTreeNode 具体实现。
- 多态：固定形状的 Recording 实现经接口引用断言只读查询与 children() 枚举顺序、
  耗尽行为（与 DefaultMutableTreeNode 对照）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
