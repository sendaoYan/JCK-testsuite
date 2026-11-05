# java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator — 设计案

## 1. API 功能
`java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：NONE, EXTENDS, SUPER

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `WildcardIndicatorTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`WildcardIndicatorTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$TypeArg$Bounded$WildcardIndicator")` + public/enum/继承 + 关键成员。黑盒（`WildcardIndicatorTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，14 → 48 断言）
- **枚举精确表**（`testEnumValueTable`）：实测 values() 恰 3 个且声明顺序 **NONE, EXTENDS, SUPER**，ordinal 0/1/2；name()==toString()；compareTo 全序；EnumSet.allOf==3。
- **valueOf 矩阵**（`testValueOfMatrix`）：3 值双向 round-trip；valueOf("BOGUS") → IAE `No enum constant …BOGUS`；valueOf("") → IAE；**valueOf(null) → NPE `Name is null`**；valueOf("extends") 大小写敏感 IAE。
- **语义映射**（`testSemanticParseMapping`）：签名串裸形/+/-/* ↔ NONE/EXTENDS/SUPER/非 Bounded；bounded(ind,·) 经 signatureString 再解析指示器保持（三指示器全过）。
