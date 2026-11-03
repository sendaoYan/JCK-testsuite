# jdk.dynalink.linker.GuardingDynamicLinker 设计案

## 1. API 功能
受保护动态链接器接口。

## 2. Java 用户如何使用
BeansLinker 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/GuardingDynamicLinker.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
