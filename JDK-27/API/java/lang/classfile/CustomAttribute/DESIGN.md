# java.lang.classfile.CustomAttribute — 设计案

## 1. API 功能
`java.lang.classfile.CustomAttribute`（class）。工厂/静态方法：(none)。查询/实例方法：attributeMapper, attributeName, toString。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `CustomAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.CustomAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`CustomAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.CustomAttribute")` + public/class/继承 + 关键成员。黑盒（`CustomAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，10→45 断言）：payload 真 round-trip+注册/未注册语义
新增 testPayloadRoundTrip（13 字节含 0xFF/0xCA/0xFE 的 payload：mapper readAttribute 用 cr.readInt+readBytes 读原始字节、writeAttribute 写回（attribute_length=4+len 布局——长度簿记错会 Reading beyond classfile bounds）、build→parse→Arrays.equals 全等、经 transform ACCEPT_ALL 二次往返仍等、空载荷）；testAllowMultiple（allowMultiple=true 注入两个同名→re-parse 2 个且各自 payload 按序全等（实测）；allowMultiple=false 单个往返正常）；testUnregisteredSemantics（未注册 parse 不抛、两个均以 UnknownAttribute 保留、名字保留、stability==UNKNOWN；transform+DROP_UNKNOWN_ATTRIBUTES 剥成 0（facts §5）；DROP_UNSTABLE_ATTRIBUTES 同样剥；注册环境对照保留 2 个）；testTypeSurface（is-a Attribute/CodeElement/ClassElement/MethodElement/FieldElement 五接口、attributeMapper() final、构造器 protected、toString 含名、可挂类/字段/方法三位置且 payload 均往返）。易错点：writeAttribute 写的 u4 是 attribute_length（载荷总长），不是业务长度字段。
