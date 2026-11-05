# jdk.dynalink.linker.support.CompositeGuardingDynamicLinker 设计案

## 1. API 功能
组合链接器：合并多个 GuardingDynamicLinker。

## 2. Java 用户如何使用
new CompositeGuardingDynamicLinker(list)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/CompositeGuardingDynamicLinker.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒空组合 getGuardedInvocation 返回 null。
## 5. 契约深化（JDK-27）
- 顺序委托保真（a→b）；首个非空结果短路后续；全拒绝→null；空组合器拒绝一切。
