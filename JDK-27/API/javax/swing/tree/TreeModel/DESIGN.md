# javax.swing.tree.TreeModel 测试设计

## 1. API 功能
树数据契约接口：8 个方法（getRoot/getChild/getChildCount/isLeaf/getIndexOfChild/valueForPathChanged/
addTreeModelListener/removeTreeModelListener），支持任意 Object 节点（不强制 TreeNode）。

## 2. Java 用户如何使用
JTree 构造 `new JTree(model)`；自定义模型（惰性加载/数据库树）实现该接口，
在变更处对监听器发 TreeModelEvent。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/TreeModel.java`；
JTree 调用点；`test/jdk/javax/swing/tree/`。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、恰好 8 个方法、全部 abstract、无常量（常量在 TreeModelEvent）。
- 已知实现：DefaultTreeModel（唯一 JDK 内置具体实现）。
- 多态：String 节点的 RecordingTreeModel（数组存子节点）经接口引用驱动：
  getChild/getChildCount/isLeaf/getIndexOfChild 往返、监听器经 fire 路径接收 INSERT/REMOVE/STRUCT。
- 易错点：getChildCount/getChild 的实现可对非 TreeNode 抛 ClassCastException（DefaultTreeModel 行为），
  接口本身不约束异常。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
