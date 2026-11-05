# javax.accessibility.AccessibleExtendedTable 测试设计

## 1. API 功能
扩展 AccessibleTable，提供 index<->(row,col) 互转。

按方法分组：
- 查询: getAccessibleRow(int), getAccessibleColumn(int), getAccessibleIndex(int,int)
- 继承: AccessibleTable 全部方法

## 2. Java 用户如何使用
列表/树/表把扁平 index 映射到行列。测试用 3x4 模型做 index/row/col 往返一致性断言。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java (AccessibleJTable)
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/AccessibleJTableTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、继承 AccessibleTable、三个新方法
- 黑盒: getAccessibleIndex(row,col)、getAccessibleRow/Column 往返一致

自动化实现位于同目录 `AccessibleExtendedTableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 继承 AccessibleTable；记录型子类线性索引换算：index=r*3+c（getAccessibleRow(5)=1、getAccessibleColumn(5)=2、getAccessibleIndex(1,2)=5）
- 真组件闭环：JTable 2x2 的 table 实为 ExtendedTable；getAccessibleIndex(1,1)=3、getAccessibleRow(2)=1、getAccessibleColumn(2)=0（行优先线性序）
