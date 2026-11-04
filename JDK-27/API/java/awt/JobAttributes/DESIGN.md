# java.awt.JobAttributes 测试设计

## 1. API 功能
打印作业属性集（份数、默认选择、目的地、对话框、页码范围、多文档处理、打印机、装订边），可 clone，含 5 个嵌套枚举。

## 2. Java 用户如何使用
new JobAttributes() / copy ctor / 11 参构造；get/set 各属性；setXToDefault 复位。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/JobAttributes.java

## 4. 测试用例设计 + 自动化实现
白盒: final、Cloneable、声明关键方法；黑盒: 默认值、set/get 往返、setXToDefault、copy/clone/equals/hashCode/toString。

自动化实现位于同目录 `JobAttributesTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
