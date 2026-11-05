# jdk.dynalink.linker.MethodHandleTransformer 设计案

## 1. API 功能
方法句柄变换器接口。

## 2. Java 用户如何使用
DefaultInternalObjectFilter 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/MethodHandleTransformer.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 单抽象方法名是 transform(MethodHandle)（非 transformMethodHandle）。
- setInternalObjectsFilter 真实闭环：仅当 beans 动态方法调用建立时（GET:METHOD→CALL）触发过滤器；LinkerServices.filterInternalObjects 直通转换保持类型。
