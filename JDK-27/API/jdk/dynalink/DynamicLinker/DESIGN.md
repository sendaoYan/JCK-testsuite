# jdk.dynalink.DynamicLinker 设计案

## 1. API 功能
动态链接器：link(callSite) + getLinkerServices()。

## 2. Java 用户如何使用
DynamicLinkerFactory().createLinker() → link(SimpleRelinkableCallSite)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/DynamicLinker.java；test/jdk/jdk/dynalink/。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 createLinker→link→getLinkerServices.canConvert。
## 5. 契约深化（JDK-27）
- 真实链接闭环：link() 返回同一 site，GET:PROPERTY:field 经 BeansLinker 求值 7。
- 未 link 的 site 调用 → IllegalStateException "uninitialized call site"；link 后未知成员 → NSDME 且 message==descriptor.toString()。
- link(null)→NPE。
- getLinkerServices() 通道：getGuardedInvocation 直接重链真实请求、getWithLookup 求值。
- getLinkedCallSiteLocation()：链接期返回调用者 StackTraceElement（类名+方法名精确断言），非链接期 null。
