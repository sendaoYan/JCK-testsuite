# java.lang.classfile.attribute.ModulePackagesAttribute — 设计案

## 1. API 功能
`java.lang.classfile.attribute.ModulePackagesAttribute`（interface）。工厂/静态方法：of, ofNames。查询/实例方法：packages。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ModulePackagesAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.ModulePackagesAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ModulePackagesAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.ModulePackagesAttribute")` + public/interface/继承 + 关键成员。黑盒（`ModulePackagesAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化条目（2026-11-05 B 批，30 断言）
- testFactoryOverloads：of(entry...)/of(List<PackageEntry>)/ofNames(varargs)/ofNames(List) 4 重载；entry 身份保留（==）；asSymbol() 点名 vs name() Utf8 斜杠名；ofNames(empty) 合法 0 包；NAME_MODULE_PACKAGES/mapper 身份/CP_REFS。
- testNegatives：of(null List)/ofNames(null) NPE。
- testInjectRoundTrip：普通类 cb.with 注入（build 不做语义校验——实测记录）；module-info 产物 transformClass endHandler 注入后 isModuleInfo 仍真 + verify 干净。
- testRealModuleInfo：jrt:/java.logging/module-info.class 真产物 packages 精确 4 包表 + PackageEntry.matches。
