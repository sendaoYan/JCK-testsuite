# jdk.dynalink.linker.support.Guards 设计案

## 1. API 功能
守卫工厂：isNull/isNotNull/isOfClass/getClassGuard 等。

## 2. Java 用户如何使用
Guards.isNull() 等静态方法。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/Guards.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒多种守卫工厂返回非空。
