# jdk.dynalink.linker.GuardingDynamicLinker 设计案

## 1. API 功能
受保护动态链接器接口。

## 2. Java 用户如何使用
BeansLinker 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/GuardingDynamicLinker.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 函数式接口单方法签名精确断言（getGuardedInvocation(LinkRequest, LinkerServices) → GuardedInvocation）。
- 真实闭环：自定义 linker 注入 DynamicLinkerFactory.setPrioritizedLinker，String 接收者返回 length 守卫调用并命中；非 String 接收者返回 null → 引发重链，最终 NSDME；观察到接收者序列证明 relink 生命周期。
