# java.lang.classfile.Opcode.Kind — 设计案

## 1. API 功能
`java.lang.classfile.Opcode.Kind`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：LOAD, STORE, INCREMENT, BRANCH, LOOKUP_SWITCH, TABLE_SWITCH, RETURN, THROW_EXCEPTION, FIELD_ACCESS, INVOKE, INVOKE_DYNAMIC, NEW_OBJECT, NEW_PRIMITIVE_ARRAY, NEW_REF_ARRAY, NEW_MULTI_ARRAY, TYPE_CHECK, ARRAY_LOAD, ARRAY_STORE, STACK, CONVERT, OPERATOR, CONSTANT, MONITOR, NOP, DISCONTINUED_JSR, DISCONTINUED_RET

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `KindTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Opcode.Kind` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`KindTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Opcode$Kind")` + public/enum/继承 + 关键成员。黑盒（`KindTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化，19 → 35 断言）
- `testValueOfSemantics`：26 名 valueOf 全量双向；不存在名 → IAE 精确消息 `No enum constant java.lang.classfile.Opcode.Kind.NO_SUCH_KIND`（**注意消息用 `.` 分隔嵌套名而非 `$`**）；valueOf(null) → NPE "Name is null"；values() 防御拷贝；ordinal 严格递增 + LOAD=0/DISCONTINUED_RET=25；toString==name。
- `testKindCoverage`：**每 kind 至少 1 个 opcode + 26 类完整精确计数表**（LOAD=30, STORE=30, OPERATOR=42, CONSTANT=20, BRANCH=18, CONVERT=15, STACK=9, ARRAY_LOAD=8, ARRAY_STORE=8, RETURN=6, FIELD_ACCESS=4, INVOKE=4, INCREMENT/TYPE_CHECK/MONITOR/DISCONTINUED_JSR/DISCONTINUED_RET=2, 九个单例=1）+ 计数和==213 + 代表成员反查。
