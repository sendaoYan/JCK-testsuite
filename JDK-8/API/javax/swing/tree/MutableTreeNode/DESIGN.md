# javax.swing.tree.MutableTreeNode 测试设计

## 1. API 功能
可变节点契约：extends TreeNode，追加 insert/removeFromParent/setParent/setUserObject 四个方法
（声明方法名 12 个：TreeNode 7 + 本接口新增 4 + Object 继承）。

## 2. Java 用户如何使用
DefaultTreeModel.insertNodeInto 要求 MutableTreeNode；自定义可变节点实现这四个方法维护父子双向链。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/MutableTreeNode.java`；
DefaultTreeModel/DefaultMutableTreeNode 调用点。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、extends TreeNode、新增 4 个方法签名精确（insert(MutableTreeNode,int) 等）。
- 多态行为矩阵：Recording 实现记录 insert/remove/setParent/setUserObject 调用，
  验证 DefaultTreeModel.insertNodeInto 会调用 child.setParent(parent)+parent.insert(child,i) 两条链；
  removeFromParent 将 parent 置 null。
- 易错点：insert 契约不要求去重/防环——那是 DefaultMutableTreeNode 的实现级行为。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
