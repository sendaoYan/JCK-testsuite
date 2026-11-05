# java.lang.classfile.attribute.AnnotationDefaultAttribute — 设计案

## 1. API 功能
`java.lang.classfile.attribute.AnnotationDefaultAttribute`（interface）。工厂/静态方法：of。查询/实例方法：defaultValue。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `AnnotationDefaultAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.AnnotationDefaultAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AnnotationDefaultAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.AnnotationDefaultAttribute")` + public/interface/继承 + 关键成员。黑盒（`AnnotationDefaultAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化）
新增维度：全 12 类型 default 值工厂矩阵（I/s/Z/J/B/C/S/F/D/e/c/[ tag 表）+ of(null) NPE + ofArray 65535/66000 u2 IAE + attributeName/attributeMapper/stability(CP_REFS)/allowMultiple(false) 元数据 + build 注入（withMethod 内 with）→ re-parse 11 元素数组逐项回读 + javac 真类 @interface 12 个带 default 方法精确值（无 default 方法无属性）。
易错点（实测）：defaultValue() 的运行时类型是 OfInt/OfString/... 需按 tag() 分派再 cast；非 @interface 类不产该属性；AnnotationValue.ofArray 与属性共享 u2 列表校验。
