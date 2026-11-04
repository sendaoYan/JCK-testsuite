# jdk.dynalink.linker.support.CompositeTypeBasedGuardingDynamicLinker 设计案

## 1. API 功能
组合类型链接器：canLinkType + optimize。

## 2. Java 用户如何使用
new CompositeTypeBasedGuardingDynamicLinker(list)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/CompositeTypeBasedGuardingDynamicLinker.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 canLinkType + optimize。
