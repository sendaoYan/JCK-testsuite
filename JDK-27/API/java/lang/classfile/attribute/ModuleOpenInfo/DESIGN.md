# java.lang.classfile.attribute.ModuleOpenInfo — 设计案

## 1. API 功能
`java.lang.classfile.attribute.ModuleOpenInfo`（interface）。工厂/静态方法：of。查询/实例方法：openedPackage, opensFlagsMask, opensFlags, has, opensTo。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ModuleOpenInfoTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.ModuleOpenInfo` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ModuleOpenInfoTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.ModuleOpenInfo")` + public/interface/继承 + 关键成员。黑盒（`ModuleOpenInfoTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化条目（2026-11-05 B 批，43 断言）
- testFactoryOverloads：8 个 of 全重载（PackageEntry/PackageDesc × int/Collection × List/varargs）；opensFlagsMask 0x1000/0x8000/0x9000；has() 三态；openedPackage().name() Utf8 斜杠形式（p/t）；AccessFlag.SYNTHETIC/MANDATED 的 MODULE_OPENS 位置。
- testNegatives：of(null pkg/modules/entry) NPE（消息可 null，仅断类型）；**构造期不校验标志位**（bad mask 0x1 保留），opensFlags() 转换时 IAE "Unmatched bit position 0x1 for location MODULE_OPENS"（实测精确消息）。
- testBuildModuleRoundTrip：ModuleAttribute.of(...mb.opens(info)/opens(pkg,mask,mods)) → buildModule → parse → opens() 包名/掩码/opensTo 表全回读 + verify 干净。
