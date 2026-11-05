# java.lang.classfile.ClassFile.AttributesProcessingOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.AttributesProcessingOption`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：PASS_ALL_ATTRIBUTES, DROP_UNKNOWN_ATTRIBUTES, DROP_UNSTABLE_ATTRIBUTES

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `AttributesProcessingOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.AttributesProcessingOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AttributesProcessingOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$AttributesProcessingOption")` + public/enum/继承 + 关键成员。黑盒（`AttributesProcessingOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：三值过滤矩阵 + 稳定性阈值实测）
新增 `testOptionSemantics()`（37 断言，12→37）。过滤规则实测（Util.isAttributeAllowed：`4? 否——5 - stability.ordinal() > processingOption.ordinal()`，AttributeStability 共 5 值 STATELESS=0/CP_REFS=1/LABELS=2/UNSTABLE=3/UNKNOWN=4，仅对 **bound** 属性生效）：
- **未知属性（未注册 parse，stability=UNKNOWN）**：PASS_ALL 保留；DROP_UNKNOWN 剥；**DROP_UNSTABLE 也剥**（UNKNOWN 排在 UNSTABLE 之后）。
- **注册过的自定义属性（parse 成用户对象，STATELESS、非 bound）**：三个选项**全保留**——readAttribute 产物不再是 BoundAttribute，过滤只作用于 bound 属性（实测锁定，易错点）。
- **真 UNSTABLE bound 属性**（自类 `static <@ApTA T> void gen(T)` 的 RuntimeVisibleTypeAnnotations，内置 mapper stability=UNSTABLE）：需强制元素重放才能观测——**NEW_POOL** 或 **transformingMethodBodies** 路径下 DROP_UNSTABLE 剥掉、PASS_ALL/DROP_UNKNOWN 保留；**SHARED_POOL + ACCEPT_ALL 走整成员字节快拷，三个选项全保留**（MethodImpl.writeTo canWriteDirect 快路径）。
- 默认（无选项）transform 行为=PASS_ALL。
- 易错点：ACCEPT_ALL+默认池下断言 DROP_UNSTABLE 剥属性会失败——必须配 NEW_POOL 或方法体 transform 才能触发过滤。
