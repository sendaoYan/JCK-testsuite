# javax.swing/table.TableColumnModel 测试设计

## 1. API 功能
列模型契约接口：addColumn/removeColumn/moveColumn/setColumnMargin/getTotalColumnWidth、
列与选择监听器注册、getColumnIndex/getColumnIndexAtX/getColumn、columnSelectionAllowed、
getSelectedColumn(s)/getSelectionModel。

## 2. Java 用户如何使用
JTable 依赖它管理列集合与列选择；自定义实现可做隐藏列/冻结列等（如 `setDefaultColumnModel`）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/TableColumnModel.java`；
JTable/JTableHeader 调用点。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、方法集合精确（含 add/removeColumnModelListener、addColumnModelListener、
  setColumnSelectionAllowed 等全部 20+ 个方法名）、全部 abstract。
- 已知实现：DefaultTableColumnModel、JTable 内部匿名用法。
- 多态：Recording 实现（Vector 记录调用）经接口引用驱动，断言调用语义与
  DefaultTableColumnModel 行为一致性（addColumn 后 getColumnCount/getTotalColumnWidth）。
- 易错点：接口在 JDK-8 无 default 方法；监听器两类（TableColumnModelListener 全套事件 +
  PropertyChangeListener 由实现负责）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
