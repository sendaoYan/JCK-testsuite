# java.lang.classfile.attribute.ModuleAttribute — 设计案

## 1. API 功能
`java.lang.classfile.attribute.ModuleAttribute`（interface）。工厂/静态方法：of。查询/实例方法：moduleName, moduleFlagsMask, moduleFlags, has, moduleVersion, requires, exports, opens, uses, provides。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ModuleAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.ModuleAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ModuleAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.ModuleAttribute")` + public/interface/继承 + 关键成员。黑盒（`ModuleAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化）
新增维度：of(ModuleDesc,builder) 全链（flags 0x1020=[OPEN,SYNTHETIC]/version/requires×2（含 TRANSITIVE+version）/exports×2（qualified 目标有序、unqualified 空 targets）/opens/uses/provides）→ buildModule → parse 全表回读 + verify 干净 + 空 builder 最小 module-info + of(ModuleEntry,builder) 条目版（uses 走 ClassEntry）+ of(ModuleEntry,int,Utf8Entry,Collection×5) 8 参版 + requires 66000 u2 IAE + buildModule(ma, cb.with(hashes)) 携带额外 class 属性 + 元数据（CP_REFS/allowMultiple false）。
易错点（实测）：ACC_MODULE flag 在 class access_flags 上；moduleVersion 传 null → Optional.empty；无目标的 exports 是「非限定导出」（targets 空）。
