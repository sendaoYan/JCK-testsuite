# javax.accessibility.AccessibleTable 测试设计

## 1. API 功能
描述表格：标题/摘要/行列数/单元格/表头/行列描述/选择。

按方法分组：
- 标题/摘要: get/setAccessibleCaption, get/setAccessibleSummary
- 维度: getAccessibleRowCount, getAccessibleColumnCount
- 单元格: getAccessibleAt, getAccessibleRowExtentAt, getAccessibleColumnExtentAt
- 表头: get/setAccessibleRowHeader, get/setAccessibleColumnHeader
- 描述: get/setAccessibleRowDescription, get/setAccessibleColumnDescription
- 选择: isAccessibleSelected/RowSelected/ColumnSelected, getSelectedAccessibleRows/Columns

## 2. Java 用户如何使用
表格组件通过它暴露结构给辅助技术。测试用 3x4 模型做标题/摘要/表头/描述 set/get 与选择断言。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java (AccessibleJTable)
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/AccessibleJTableTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、关键 getter/setter
- 黑盒: 维度/标题摘要表头描述 set-get/选择断言

自动化实现位于同目录 `AccessibleTableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- caption/summary 为 Accessible 类型（非 String）——签名级契约；记录型子类 RecTable 全 20 方法面
- ModelChange 常量：INSERT=1、UPDATE=0、DELETE=-1
- 真组件闭环：JTable(2x1) 暴露 table，rows=2/cols=1；ExtendedTable 子接口关系
