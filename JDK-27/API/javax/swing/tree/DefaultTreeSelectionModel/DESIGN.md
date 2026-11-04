# javax.swing.tree.DefaultTreeSelectionModel 测试设计

## 1. API 功能
树选择默认实现：SINGLE_TREE/SINGLE_INTERVAL/ DISCONTIGUOUS_TREE 三模式、
set/add/removeSelectionPath(s)、getSelectionPaths/Count、isPathSelected/isSelectedPath、
RowMapper 行映射、resetRowSelection、lead/anchor 路径、选择监听器与 propertyChange。

## 2. Java 用户如何使用
JTree 内部持有；程序侧 `tree.getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION)`；
监听 TreeSelectionEvent 的 getPaths()+isAdded() 做增量 UI。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/DefaultTreeSelectionModel.java`；
`test/jdk/javax/swing/tree/DefaultTreeSelectionModel/bug4485322.java`。

## 4. 测试用例设计 + 易错细节
- 默认：DISCONTIGUOUS 模式、空选择时 getSelectionPaths() 返回**空数组而非 null**、
  getSelectionCount()==0、lead/anchor null。
- SINGLE 模式 setSelectionPaths 两个路径只保留第一个；SINGLE_INTERVAL 丢弃不连续段；
  DISCONTIGUOUS 全保留；addSelectionPath 并集、removeSelectionPath 差集。
- setSelectionMode(99/-5) 不抛异常而被**钳到 DISCONTIGUOUS(4)**（与 ListSelectionModel 的 IAE 相反）。
- RowMapper：自定义映射 rows=[1,2] 后 getMinSelectionRow/getMaxSelectionRow/getLeadSelectionRow；
  resetRowSelection 清行缓存并重查。
- 事件：精确 TreeSelectionEvent（paths+isAdded 逐位断言）；冗余 set 同一选择不投递；
  isAddedPath(path) 在非事件上下文抛 IllegalArgumentException（"path is not a selection path"）。
- SELECTION_MODE_PROPERTY="selectionMode" 常量在 Default 实现上（接口无此常量）。
- 序列化：DefaultMutableTreeNode equals 为身份比较，round-trip 后断言反序列化模型自身路径选中。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
