# jdk.dynalink.DynamicLinkerFactory 设计案

## 1. API 功能
链接器工厂：set* 配置 + createLinker()。

## 2. Java 用户如何使用
setPrioritizedLinkers/setSyncOnRelink/setUnstableRelinkThreshold → createLinker。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/DynamicLinkerFactory.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒多步配置后 createLinker 两次。
