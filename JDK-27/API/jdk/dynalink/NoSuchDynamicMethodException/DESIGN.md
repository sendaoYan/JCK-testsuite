# jdk.dynalink.NoSuchDynamicMethodException 设计案

## 1. API 功能
动态方法不存在异常。

## 2. Java 用户如何使用
new NoSuchDynamicMethodException(msg/cause)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/NoSuchDynamicMethodException.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 message/多实例/hierarchy。
## 5. 契约深化（JDK-27）
- serialVersionUID==1L（ObjectStreamClass.lookup 读取）；无 cause 通道。
- 引擎真实抛出路径：不可链接 site 的 message 为 descriptor.toString()（前缀 "GET:PROPERTY:missing(Object)Object@"）。
