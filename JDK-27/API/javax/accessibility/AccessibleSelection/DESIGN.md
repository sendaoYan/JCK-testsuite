# javax.accessibility.AccessibleSelection 测试设计

## 1. API 功能
定义对象的子项选择：选择数量、是否选中、增删清空/全选。

按方法分组：
- 查询: getAccessibleSelectionCount(), getAccessibleSelection(int), isAccessibleChildSelected(int)
- 操作: add/removeAccessibleSelection(int), clearAccessibleSelection(), selectAllAccessibleSelection()

## 2. Java 用户如何使用
列表/树/表用它管理子项选择。测试用 BitSet 模型做选择增删、全选、清空组合断言。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JList.java (AccessibleJList)
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/AccessibleJListTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、七个方法
- 黑盒: 初始 0；add/remove 选择计数与状态；selectAll；clear

自动化实现位于同目录 `AccessibleSelectionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 完整方法面 6 方法（含 selectAllAccessibleSelection）；记录型子类 isAccessibleChildSelected 分支
- 真组件对照：JLabel 无选择（null）；JList 暴露选择且初始 0；setSelectedIndex(1) 后 count=1、child1=true、child0=false
