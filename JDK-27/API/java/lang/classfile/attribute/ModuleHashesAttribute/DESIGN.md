# java.lang.classfile.attribute.ModuleHashesAttribute — 设计案

## 1. API 功能
`java.lang.classfile.attribute.ModuleHashesAttribute`（interface）。工厂/静态方法：of。查询/实例方法：algorithm, hashes。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ModuleHashesAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.ModuleHashesAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ModuleHashesAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.ModuleHashesAttribute")` + public/interface/继承 + 关键成员。黑盒（`ModuleHashesAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化）
新增维度：四工厂（String+List/String+varargs/Utf8Entry+List/Utf8Entry+varargs，Utf8Entry 版保持条目 identity）+ 空 hash 表 + 三种 null NPE + 65535/66000 u2 IAE + 元数据（CP_REFS/allowMultiple false）+ buildModule(ma, cb.with(ha)) 注入 → 回读（algorithm/每条 module+字节）+ 普通 class 注入容忍。
