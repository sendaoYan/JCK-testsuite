# jdk.dynalink.linker.GuardedInvocation 设计案

## 1. API 功能
受保护调用：invocation+guard+switchpoints+asType/compose。

## 2. Java 用户如何使用
new GuardedInvocation(mh, guard)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/GuardedInvocation.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+asType+compose+replaceMethods+addSwitchPoint。
## 5. 契约深化（JDK-27）
- 生命周期：五构造器全达；SwitchPoint 失效前后 hasBeenInvalidated；多 switchpoint+exception 构造保真；getException 默认 null。
- replaceMethods 保留 switchpoints/exception 且不可变；addSwitchPoint 追加、null 返回 this。
- 适配：asType（返回类型）、filterArguments、dropArguments（两种重载）。
- compose(fallback)：守卫命中/回退两路真实求值；类型不匹配的精确 IAE 消息（"target and fallback types must match: ..." / "target and test types must match: ..."）。
