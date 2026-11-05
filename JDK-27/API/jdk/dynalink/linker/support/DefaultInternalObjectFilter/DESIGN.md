# jdk.dynalink.linker.support.DefaultInternalObjectFilter 设计案

## 1. API 功能
内部对象过滤器：transform。

## 2. Java 用户如何使用
new DefaultInternalObjectFilter(param, ret)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/DefaultInternalObjectFilter.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 transform 非空。
## 5. 契约深化（JDK-27）
- (null, null) 构造合法（无参校验）。
- 真实变换闭环：参数过滤器去 '#' 前缀 + 返回过滤器替换值，transform 后双通道生效且原 handle 不变。
- implements MethodHandleTransformer 断言。
