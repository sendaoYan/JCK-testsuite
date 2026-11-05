# java.lang.classfile.AttributeMapper.AttributeStability — 设计案

## 1. API 功能
`java.lang.classfile.AttributeMapper.AttributeStability`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：STATELESS, CP_REFS, LABELS, UNSTABLE, UNKNOWN

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `AttributeStabilityTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.AttributeMapper.AttributeStability` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AttributeStabilityTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.AttributeMapper$AttributeStability")` + public/enum/继承 + 关键成员。黑盒（`AttributeStabilityTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，12→48 断言）：枚举次序+落点表
新增 testEnumOrderAndValueOf（声明次序即 ordinal 次序 STATELESS(0)<CP_REFS(1)<LABELS(2)<UNSTABLE(3)<UNKNOWN(4)、compareTo 严格全序、valueOf 双向逐值、valueOf("RELAXED") IAE 精确消息、valueOf(null) NPE）；testStandardStabilities（12+ 标准属性 stability 落点精确表：Deprecated/Synthetic STATELESS、SourceFile/Signature/ConstantValue/Code/Exceptions/NestHost/Record/Module/InnerClasses CP_REFS、StackMapTable=LABELS（实测，非 UNSTABLE）、唯二 UNSTABLE=TypeAnnotation 族）；testUnknownStabilityFromUnregisteredParse（未注册自定义属性 parse 保留且 mapper.stability()==UNKNOWN——实测）。易错点：本 JDK 枚举为 5 值含 UNKNOWN（未注册属性专用），非传闻 4 值。
