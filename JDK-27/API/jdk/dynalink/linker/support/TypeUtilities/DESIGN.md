# jdk.dynalink.linker.support.TypeUtilities 设计案

## 1. API 功能
类型工具：isSubtype/getWrapperType/getPrimitiveType 等。

## 2. Java 用户如何使用
TypeUtilities.isSubtype(...) 等静态方法。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/TypeUtilities.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒多组类型判断断言。
