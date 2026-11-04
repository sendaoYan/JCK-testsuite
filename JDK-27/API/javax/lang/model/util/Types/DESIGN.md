# javax.lang.model.util.Types — 设计方案

## 1. API 功能
javax.lang.model 的实用工具：Elements 查询元素/包/模块/注解/文档注释/覆写关系；Types 做类型关系判断与类型构造；ElementFilter 从混合元素集合中筛出字段/方法/构造器/类型/指令。

## 2. Java 用户如何使用
编译一段固定源码后用 `JavacTask.getElements()/getTypes()` 取工具，`getTypeElement`/`getAllMembers`/`isSubtype`/`boxedClass`/`fieldsIn` 等组合调用，排序后断言。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/util/Types.java` 与 `test/jdk/langtools/tools/javac/` 编译器测试；javac 自身 `Enter`/`Attr` 亦通过 Types/Elements 完成类型比较与成员解析。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言接口/抽象方法形态；黑盒解析固定源码后对确定性元素/类型列表排序断言（getTypeElement、getAllMembers、isSubtype/erasure/boxedClass/getArrayType、fieldsIn/methodsIn 等），并覆盖未知输入/非法参数异常。 对应 `TypesTest.java` 的 `testBlackBox()`。
