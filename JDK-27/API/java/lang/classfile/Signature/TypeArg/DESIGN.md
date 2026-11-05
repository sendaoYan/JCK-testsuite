# java.lang.classfile.Signature.TypeArg — 设计案

## 1. API 功能
`java.lang.classfile.Signature.TypeArg`（interface）。工厂/静态方法：of, unbounded, extendsOf, superOf, bounded。查询/实例方法：(none)。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TypeArgTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.TypeArg` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypeArgTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$TypeArg")` + public/interface/继承 + 关键成员。黑盒（`TypeArgTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，11 → 66 断言）
- **工厂全表**（`testFactoryMatrix`）：of→NONE、extendsOf→EXTENDS、superOf→SUPER、bounded(每个指示器,·)、unbounded() **单例（== 与 equals 双断言，实测 INSTANCE）**；boundType 三种 RefTypeSig（类/TypeVar/数组）。
- **null 负例全表**（`testNullMatrix`）：of/extendsOf/superOf/bounded(null,·)/bounded(ind,null) 5 处全 NPE（实测）。
- **嵌入解析矩阵**（`testEmbeddingMatrix`）：裸形/+/-/*/TypeVar/数组实参逐型 instanceof + bound 值；Map 双实参。
- **组合发射**（`testCompositionEmission`）：ClassTypeSig.of 携带各形态实参 → 精确串（`<…>`/`<+…>`/`<-…>`/`<*>`/`<*Ljava/lang/String;>`）+ 发射产物 re-parse 等值。
- **equals/hashCode**（`testEqualityContract`）：跨工厂等值（extendsOf==bounded(EXTENDS,)）、NONE vs EXTENDS 不等、Bounded vs Unbounded 不等。
- **真类闭环**（`testRealClassGenericLoop`）：javac `List<String>`/`List<?>` 字段 → 实参 Bounded/Unbounded 分型。
