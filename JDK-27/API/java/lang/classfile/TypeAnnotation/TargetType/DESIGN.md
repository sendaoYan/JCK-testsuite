# java.lang.classfile.TypeAnnotation.TargetType — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.TargetType`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：targetTypeValue, sizeIfFixed。枚举常量：CLASS_TYPE_PARAMETER, METHOD_TYPE_PARAMETER, CLASS_EXTENDS, CLASS_TYPE_PARAMETER_BOUND, METHOD_TYPE_PARAMETER_BOUND, FIELD, METHOD_RETURN, METHOD_RECEIVER, METHOD_FORMAL_PARAMETER, THROWS, LOCAL_VARIABLE, RESOURCE_VARIABLE, EXCEPTION_PARAMETER, INSTANCEOF, NEW, CONSTRUCTOR_REFERENCE, METHOD_REFERENCE, CAST, CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT, METHOD_INVOCATION_TYPE_ARGUMENT, CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT, METHOD_REFERENCE_TYPE_ARGUMENT

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `TargetTypeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.TargetType` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TargetTypeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$TargetType")` + public/enum/继承 + 关键成员。黑盒（`TargetTypeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，15 → 183 断言）
- `testEnumMatrix()`：22 个常量按声明顺序全表断言 `name()/ordinal()/targetTypeValue()/sizeIfFixed()`，`valueOf` 双向、`toString` 含 name、`compareTo` 顺序、`equals/hashCode/getDeclaringClass` Enum 契约、`values()` 每次返回新数组且内容等价、`valueOf("NOPE")` IAE（消息 "No enum constant ..."）、`valueOf(null)` NPE。
- 反射对照：以 `TargetInfo` 的 22 个 `TARGET_*` int 常量逐一核对 `targetTypeValue()`（22/22 全对上），锁死枚举与 JVMS 4.7.20 target_type 编码的一致性。
- 实测锁定值集：CLASS_TYPE_PARAMETER=0, METHOD_TYPE_PARAMETER=1, CLASS_EXTENDS=16, CLASS_TYPE_PARAMETER_BOUND=17, METHOD_TYPE_PARAMETER_BOUND=18, FIELD=19, METHOD_RETURN=20, METHOD_RECEIVER=21, METHOD_FORMAL_PARAMETER=22, THROWS=23, LOCAL_VARIABLE=64, RESOURCE_VARIABLE=65, EXCEPTION_PARAMETER=66, INSTANCEOF=67, NEW=68, CONSTRUCTOR_REFERENCE=69, METHOD_REFERENCE=70, CAST=71, CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT=72, METHOD_INVOCATION_TYPE_ARGUMENT=73, CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT=74, METHOD_REFERENCE_TYPE_ARGUMENT=75；sizeIfFixed 依次 {1,1,2,2,2,0,0,0,1,2,-1,-1,2,2,2,2,2,3,3,3,3,3}（LOCAL_VARIABLE/RESOURCE_VARIABLE 为 -1 变长）。

