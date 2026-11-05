# java.lang.classfile.AttributeMapper — 设计案

## 1. API 功能
`java.lang.classfile.AttributeMapper`（interface）。工厂/静态方法：(none)。查询/实例方法：name, readAttribute, writeAttribute, allowMultiple, stability。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `AttributeMapperTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.AttributeMapper` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AttributeMapperTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.AttributeMapper")` + public/interface/继承 + 关键成员。黑盒（`AttributeMapperTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，12→353 断言）：注册表全量+稳定性精确表
新增 testFullRegistry（36 个 mapper 工厂全量反射循环：非 null/name()==NAME_* 常量/规范字面量/重复调用同实例（==）/stability 非 null，≥30 组配对；反射计数恰 36 工厂+36 常量（javap 锁定））；testStabilityAndAllowMultipleTable（stability 精确表：deprecated/synthetic/moduleResolution/sourceDebugExtension=STATELESS、sourceFile/signature/code/…=CP_REFS、stackMapTable/lineNumberTable/localVariableTable/localVariableTypeTable/characterRangeTable=LABELS、唯二 UNSTABLE=runtime{Visible,Invisible}TypeAnnotations；闭域断言标准属性无 UNKNOWN；allowMultiple 恰 6 个 true（deprecated/synthetic/characterRangeTable/lineNumberTable/localVariableTable/localVariableTypeTable）——全表经独立探针实测）；testCustomMapperInterface（readAttribute/writeAttribute/stability/allowMultiple 声明白盒+default 标记、自造最小 mapper 的 allowMultiple 默认 false、自造 mapper 作 findAttribute 键闭环）。易错点：brief 所称 4 值 STATELESS/RELAXED/LABILE/UNSTABLE 与本 JDK 不符——实测为 STATELESS/CP_REFS/LABELS/UNSTABLE/UNKNOWN 5 值。
