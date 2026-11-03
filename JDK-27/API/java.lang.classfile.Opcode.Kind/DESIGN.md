# java.lang.classfile.Opcode.Kind — 设计案

## 1. API 功能
`java.lang.classfile.Opcode.Kind`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：LOAD, STORE, INCREMENT, BRANCH, LOOKUP_SWITCH, TABLE_SWITCH, RETURN, THROW_EXCEPTION, FIELD_ACCESS, INVOKE, INVOKE_DYNAMIC, NEW_OBJECT, NEW_PRIMITIVE_ARRAY, NEW_REF_ARRAY, NEW_MULTI_ARRAY, TYPE_CHECK, ARRAY_LOAD, ARRAY_STORE, STACK, CONVERT, OPERATOR, CONSTANT, MONITOR, NOP, DISCONTINUED_JSR, DISCONTINUED_RET

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `KindTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Opcode.Kind` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`KindTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Opcode$Kind")` + public/enum/继承 + 关键成员。黑盒（`KindTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。
