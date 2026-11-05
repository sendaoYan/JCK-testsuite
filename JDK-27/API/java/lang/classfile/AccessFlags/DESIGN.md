# java.lang.classfile.AccessFlags — 设计案

## 1. API 功能
`java.lang.classfile.AccessFlags`（interface）。工厂/静态方法：(none)。查询/实例方法：flagsMask, flags, has, location。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `AccessFlagsTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.AccessFlags` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AccessFlagsTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.AccessFlags")` + public/interface/继承 + 关键成员。黑盒（`AccessFlagsTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化，17 → 61 断言）
- 白盒追加：AccessFlags 同时 extends ClassElement+MethodElement+FieldElement（javap 锁定）；declares location。
- `testFlagMatrix`：build 注入 ACC_PUBLIC|ACC_STATIC|ACC_FINAL 等回读——class/field/method 三级 location() 精确（CLASS/FIELD/METHOD）；flagsMask 位与（0x21/0x19/0xc2/0x79）；flags() Set 精确内容；has() 逐位；**toString 格式实测锁定为 `AccessFlags[flags=33]`（十进制掩码，不含标志名文本）**；flags() 返回不可变 Set（add → UnsupportedOperationException）；has(null) → NPE（消息含 `"flag" is null`）；AccessFlags 元素在三级 elementList 中 instanceof 可见。
- `testInvalidBits`：**ACC_STRICT(0x800) v71 已从 METHOD 合法位移除**——flagsMask 原样保留 0x809，flags() 抛 IAE 精确消息 `Unmatched bit position 0x800 for location METHOD`，has(PUBLIC) 仍 true，toString=`AccessFlags[flags=2057]`；ACC_INTERFACE(0x200) 位在 METHOD 同型（IAE `Unmatched bit position 0x200 for location METHOD`，toString 513）。
- `testLocationVariants`：withFlags(int) 建接口类（INTERFACE|ABSTRACT 位在 CLASS 位置合法，mask 0x600）；withFlags(AccessFlag...) 变体（javap 锁定第二重载）；0 标志字段 → 空 set；抽象方法（无 code）flags 回读；AccessFlag.Location.values()==9。
