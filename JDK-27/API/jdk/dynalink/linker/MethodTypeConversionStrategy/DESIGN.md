# jdk.dynalink.linker.MethodTypeConversionStrategy 设计案

## 1. API 功能
方法类型转换策略接口。

## 2. Java 用户如何使用
工厂 setAutoConversionStrategy。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/MethodTypeConversionStrategy.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 单抽象 asType(MethodHandle, MethodType)。
- setAutoConversionStrategy 真实闭环：链接期对 invocation/guard 各至少一次调用，目标类型收敛到 site 类型。
