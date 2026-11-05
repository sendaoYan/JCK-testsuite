# jdk.dynalink.support.ChainedCallSite 设计案

## 1. API 功能
链式调用点：relink/resetAndRelink。

## 2. Java 用户如何使用
new ChainedCallSite(csd)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/support/ChainedCallSite.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+type+getDescriptor。
## 5. 契约深化（JDK-27）
- 真实链式闭环：GET:ELEMENT 单 site 依次服务 List/数组两种接收者，旧分支仍可用（"b"→"y"→"a"）。
- 不支持接收者触发 NSDME 后链不破坏（继续 "x"）。
- resetAndRelink 丢弃累积链；getMaxChainLength 为 protected int（默认 8，无 open 不可反射调用）。
