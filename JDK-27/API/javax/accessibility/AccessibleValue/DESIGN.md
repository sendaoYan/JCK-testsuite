# javax.accessibility.AccessibleValue 测试设计

## 1. API 功能
数值访问：当前值、最小值、最大值，并支持设置当前值。

按方法分组：
- 查询: getCurrentAccessibleValue, getMinimumAccessibleValue, getMaximumAccessibleValue
- 设置: setCurrentAccessibleValue(Number) -> boolean

## 2. Java 用户如何使用
进度条/滑块等通过它暴露数值。测试用匿名实现做 set/get 当前值与边界断言。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JProgressBar.java (AccessibleJProgressBar)
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、四个方法
- 黑盒: 当前/最小/最大值；setCurrent 回读

自动化实现位于同目录 `AccessibleValueTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 记录型子类四方法（Number 语义，含 BigInteger/Integer 混用）；setCurrentAccessibleValue(null) false
- 真组件对照：JButton 暴露 value 且三元组 0/0/1；JLabel 无 value（null）
