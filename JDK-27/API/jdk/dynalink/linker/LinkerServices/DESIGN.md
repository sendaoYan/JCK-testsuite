# jdk.dynalink.linker.LinkerServices 设计案

## 1. API 功能
链接服务接口：asType/canConvert/getGuardedInvocation 等。

## 2. Java 用户如何使用
DynamicLinker.getLinkerServices()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/LinkerServices.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
