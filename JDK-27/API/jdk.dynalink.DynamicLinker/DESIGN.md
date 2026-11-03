# jdk.dynalink.DynamicLinker 设计案

## 1. API 功能
动态链接器：link(callSite) + getLinkerServices()。

## 2. Java 用户如何使用
DynamicLinkerFactory().createLinker() → link(SimpleRelinkableCallSite)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/DynamicLinker.java；test/jdk/jdk/dynalink/。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 createLinker→link→getLinkerServices.canConvert。
