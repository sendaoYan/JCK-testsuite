# javax.swing.tree.TreeSelectionModel 测试设计

## 1. API 功能
树选择契约接口：27 个方法（三模式常量 SINGLE_TREE=1/SINGLE_INTERVAL=2/DISCONTIGUOUS=4，
set/get/add/removeSelectionPath(s)，RowMapper 挂接，lead/anchor 查询，选择与属性监听器）。

## 2. Java 用户如何使用
JTree.getSelectionModel() 取；`setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)` 限单选；
TreeSelectionListener.valueChanged 里 getNewLeadSelectionPath 更新 UI。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/TreeSelectionModel.java`；
JTree 调用点。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、恰好 27 个方法（含 add/removePropertyChangeListener）、全部 abstract、
  三个模式常量精确值 1/2/4 且 public static final。
- 易错点：接口**无** getLastSelectedPathComponent（那是 JTree 的便捷方法）；
  SELECTION_MODE_PROPERTY 常量在 DefaultTreeSelectionModel 而非接口（两侧均如此）。
- 多态：Recording 实现经接口引用驱动选择/清除/监听器注册与 RowMapper 回调。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
