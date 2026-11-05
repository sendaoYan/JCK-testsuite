# java.lang.classfile.ClassFile.AttributeMapperOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.AttributeMapperOption`（interface）。工厂/静态方法：of。查询/实例方法：attributeMapper。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `AttributeMapperOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.AttributeMapperOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AttributeMapperOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$AttributeMapperOption")` + public/interface/继承 + 关键成员。黑盒（`AttributeMapperOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：mapper 函数契约实测）
新增 `testOptionSemantics()`（35 断言，10→35），自定义 ApAttr/STATELESS mapper：
- **of(fn) 双 getter 身份**：`attributeMapper() == fn`（同实例回读）；两个 option 各持各的 fn；`of(null)` → NPE（requireNonNull，无详情消息）。
- **fn 惰性求值**：parse 后不触 fn，**访问 attributes() 时才按属性名 Utf8Entry 咨询**（计数器实测 consulted≥1 且见到 "AmoAttr" 名）——断言 fn 被调必须先 materialize attributes。
- **注册 round-trip**：build(注册)→parse(注册) → ProbeAttribute 实例、`attributeMapper()` 身份、name/stability 回读。
- **未注册 parse 不抛**：保留为 UnknownAttribute（BoundUnknownAttribute），attributeName 保真；**fn 返回 null 走同一默认路径**。
- rebuild 字节大小稳定；与 DROP_DEBUG 组合成新实例仍解析自定义属性。
- 易错点：fn 在 build 侧用于写入、parse 侧用于读取，两侧同名都要覆盖；null 返回值=「不认识」，不抛异常。
