# java.lang.classfile.Signature.TypeArg.Bounded — 设计案

## 1. API 功能
`java.lang.classfile.Signature.TypeArg.Bounded`（interface）。工厂/静态方法：(none)。查询/实例方法：wildcardIndicator, boundType。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `BoundedTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.TypeArg.Bounded` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`BoundedTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$TypeArg$Bounded")` + public/interface/继承 + 关键成员。黑盒（`BoundedTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，10 → 55 断言）
- **指示器 × 来源双矩阵**（`testIndicatorMatrix`）：工厂通道（of/extendsOf/superOf/bounded 全指示器）+ 解析通道（裸形/+/- 三串）→ wildcardIndicator().name() 与 boundType() 全等断言。
- **boundType 三形态**（`testBoundTypeKinds`）：类/TypeVar/数组 bound（工厂+解析双通道，含 `List<[I>;` 数组实参、嵌套泛型类 bound）。
- **深嵌套通配**（`testNestedWildcard`）：`List<List<+Number;>;>` 外层 NONE/内层 EXTENDS 分层断言。
- **null 负例**（`testNullMatrix`）：bounded(null,·)/bounded(ind,null)/extendsOf(null) 全 NPE。
- **equals/hashCode**（`testEqualityContract`）：of==bounded(NONE,)、superOf==bounded(SUPER,)、解析实参==工厂实参。
- **真类闭环**（`testRealClassFieldLoop`）：**javac `List<? extends Number>` → `<+Ljava/lang/Number;>` EXTENDS、`List<? super String>` → `<-Ljava/lang/String;>` SUPER**，逐字段精确串 + 指示器 + boundType。
- 访问器名实测：**`wildcardIndicator()`/`boundType()`**（不是 typeVar/classType/signature）。
