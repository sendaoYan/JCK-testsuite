# java.lang.classfile.Signature.TypeArg.Unbounded — 设计案

## 1. API 功能
`java.lang.classfile.Signature.TypeArg.Unbounded`（interface）。工厂/静态方法：(none)。查询/实例方法：(none)。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `UnboundedTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.TypeArg.Unbounded` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`UnboundedTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$TypeArg$Unbounded")` + public/interface/继承 + 关键成员。黑盒（`UnboundedTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，6 → 47 断言）
- **标记接口 + 单例**（`testMarkerAndSingleton`）：实测无自有方法、直接 extends TypeArg；**unbounded() 是单例（==、equals、hashCode 稳定）**；instanceof TypeArg 是、Bounded 否。
- **解析嵌入矩阵**（`testParseEmbedding`）：`List<*>` 单实参；**`Map<**>` 双无界**；混合 `Map<*Ljava/lang/String;>` 分型；嵌套 `List<List<*>;>` 内层 Unbounded；4 串 round-trip 幂等。
- **组合发射**（`testCompositionEmission`）：of(List, unbounded)→`Ljava/util/List<*>;`、of(Map, unbounded×2)→`Ljava/util/Map<**>;`，发射产物 re-parse 回到 Unbounded 且 equals unbounded()；数组元素位含无界通配。
- **真类闭环**（`testRealClassFieldLoop`）：**javac `List<?>` → `Ljava/util/List<*>;`、`Map<?,?>` → `Ljava/util/Map<**>;`**，逐实参 instanceof Unbounded。
