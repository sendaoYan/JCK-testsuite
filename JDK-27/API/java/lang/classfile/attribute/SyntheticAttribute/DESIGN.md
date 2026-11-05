# java.lang.classfile.attribute.SyntheticAttribute — 设计案

## 1. API 功能
`java.lang.classfile.attribute.SyntheticAttribute`（interface）。工厂/静态方法：of。查询/实例方法：(none)。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `SyntheticAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.SyntheticAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`SyntheticAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.SyntheticAttribute")` + public/interface/继承 + 关键成员。黑盒（`SyntheticAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化条目（2026-11-05 B 批，24 断言）
- testInjectRoundTrip：of() 类/字段/方法三级注入 → re-parse 存在 + verify 干净。
- testAttributeSurface：NAME_SYNTHETIC/STATELESS/**allowMultiple=true（双注入两个都保留——实测）**。
- testRealProduct：javac v49+ 用 ACC_SYNTHETIC 标志而非属性——enum $VALUES 字段有标志无属性、values() mask==9 无属性、本类无属性（三重负空间断言）。
