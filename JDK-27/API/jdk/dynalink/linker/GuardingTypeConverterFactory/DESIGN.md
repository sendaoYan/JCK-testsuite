# jdk.dynalink.linker.GuardingTypeConverterFactory 设计案

## 1. API 功能
类型转换工厂接口。

## 2. Java 用户如何使用
由链接服务实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/GuardingTypeConverterFactory.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 单抽象 convertToType(Class, Class, Supplier<Lookup>) 三参形态（JDK15+ lookup supplier）。
- 真实闭环：同时实现 GuardingDynamicLinker+本接口的 linker 注入后 canConvert(String,int) 变 true、getTypeConverter 返回自定义转换器并可求值；未覆盖类型对保持 false。
