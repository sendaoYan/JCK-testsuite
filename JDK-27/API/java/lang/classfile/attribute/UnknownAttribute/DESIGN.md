# java.lang.classfile.attribute.UnknownAttribute — 设计案

## 1. API 功能
`java.lang.classfile.attribute.UnknownAttribute`（interface）。工厂/静态方法：(none)。查询/实例方法：contents。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `UnknownAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.UnknownAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`UnknownAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.UnknownAttribute")` + public/interface/继承 + 关键成员。黑盒（`UnknownAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化条目（2026-11-05 B 批，24 断言）
- testUnknownPresentation：自定义 mapper build（类+方法级）→ 无 mapper parse → **以 UnknownAttribute 呈现**：attributeName=="MyAttr"、contents==payload 逐字节（该类唯一自然产生通道）。
- testOptionCross：DROP_UNKNOWN_ATTRIBUTES transform 剥除；注册 mapper 的 parse 走 readAttribute 自定义通道（非 Unknown）；AttributesProcessingOption 3 值。
- testWhiteBoxSurface：四 element 面（Class/Method/Field/Code）；**Attributes 无 unknown() mapper 方法**（只能 instanceof 发现——白盒负断言）。
