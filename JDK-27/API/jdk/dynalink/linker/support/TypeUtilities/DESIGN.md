# jdk.dynalink.linker.support.TypeUtilities 设计案

## 1. API 功能
类型工具：isSubtype/getWrapperType/getPrimitiveType 等。

## 2. Java 用户如何使用
TypeUtilities.isSubtype(...) 等静态方法。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/TypeUtilities.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒多组类型判断断言。
## 5. 契约深化（JDK-27）
- 方法调用转换矩阵：拓宽/装箱/拆箱 true，窄化 false，String↔int false，引用宽化 true。
- 无损转换矩阵：int→long true、long→int false、宽化引用 true、下转 false。
- isSubtype：引用继承 true、原始类型拓宽也算 subtype（int<:long、long<:double）、原始与包装互不相关、自反。
- 原始类型映射：九个原始类型名 round-trip；void 的包装是 Void（isWrapperType(Void) true、getPrimitiveType(Void)==void）；未知名/非包装→null。
