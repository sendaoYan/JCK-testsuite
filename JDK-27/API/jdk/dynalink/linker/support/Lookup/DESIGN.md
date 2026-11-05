# jdk.dynalink.linker.support.Lookup 设计案

## 1. API 功能
查找工具：findStatic/findVirtual/unreflect/findSpecial。

## 2. Java 用户如何使用
new Lookup(lookup) + findStatic。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/Lookup.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 findStatic/findVirtual/findSpecial。
## 5. 契约深化（JDK-27）
- 全方法面真实行为：PUBLIC 常量 unreflect；findOwnStatic/findStatic/findVirtual/findGetter/unreflectGetter/unreflectSetter/unreflectConstructor 类型精确断言；findSpecial/findOwnSpecial 可用。
- 失败通道：不可访问的 special 方法 → IllegalAccessError（受检异常转链接错误）。
- 静态变体 unreflect/unreflectConstructor/findOwnStatic 同效。
