# javax.swing.tree.RowMapper 测试设计

## 1. API 功能
单方法接口：`int[] getRowsForPaths(TreePath[] paths)`——把树路径映射到显示行，
供 TreeSelectionModel 维护行级选择缓存。

## 2. Java 用户如何使用
`selectionModel.setRowMapper(mapper)`；自定义 UI（如 FixedHeightLayoutCache）实现它
批量换算行号。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/RowMapper.java`；
AbstractLayoutCache/FixedHeightLayoutCache 实现；DefaultTreeSelectionModel.setRowMapper 调用点。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、恰好 1 个抽象方法（返回 int[]、入参 TreePath[]）、无常量。
- 集成断言：DefaultTreeSelectionModel.setRowMapper(RowMapper.class) 可注入，
  注入后 getMinSelectionRow 等行查询走 mapper 回调。
- 多态：固定映射 {p1→1, p2→2} 的实现经接口引用驱动，断言数组内容与长度。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
