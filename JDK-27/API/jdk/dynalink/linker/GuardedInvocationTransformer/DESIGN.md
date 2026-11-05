# jdk.dynalink.linker.GuardedInvocationTransformer 设计案

## 1. API 功能
受保护调用变换器接口。

## 2. Java 用户如何使用
由工厂 prelink 使用。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/GuardedInvocationTransformer.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 单抽象 filter(GuardedInvocation, LinkRequest, LinkerServices)。
- setPrelinkTransformer 真实闭环：包装返回值（filterReturnValue 前缀标签 "T:name"）、观察到操作串 "GET:PROPERTY:name"；返回 null → relink 期 NPE。
- 默认 prelink 为 asType 适配（未设置时链接照常）。
