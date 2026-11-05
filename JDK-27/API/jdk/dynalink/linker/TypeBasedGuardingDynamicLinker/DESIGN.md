# jdk.dynalink.linker.TypeBasedGuardingDynamicLinker 设计案

## 1. API 功能
基于类型的链接器接口：canLinkType。

## 2. Java 用户如何使用
CompositeTypeBasedGuardingDynamicLinker 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/TypeBasedGuardingDynamicLinker.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 继承 GuardingDynamicLinker 并仅新增 canLinkType。
- 组合器优化真实闭环：canLinkType-false 委托按接收者类型被跳过（canLinkType 调用序列不含 Integer）。
