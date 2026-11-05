# java.lang.classfile.Signature.RefTypeSig — 设计案

## 1. API 功能
`java.lang.classfile.Signature.RefTypeSig`（interface）。工厂/静态方法：(none)。查询/实例方法：(none)。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `RefTypeSigTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.RefTypeSig` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`RefTypeSigTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$RefTypeSig")` + public/interface/继承 + 关键成员。黑盒（`RefTypeSigTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，7 → 47 断言）
- **标记接口白盒**（`testMarkerInterfaceShape`）：实测无自有方法（getDeclaredMethods()==0）、直接 extends Signature；isAssignableFrom 矩阵（ClassTypeSig/ArrayTypeSig/TypeVarSig 是，**BaseTypeSig 否**）。
- **三实现多态矩阵**（`testPolymorphismMatrix`）：parseFrom 与工厂双通道，3 类实例 signatureString 多态分派 + 精确 kind。
- **四类消费位**（`testUsagePositions`）：TypeArg.bounded 的 boundType / TypeParam classBound / ArrayTypeSig componentSignature / throws 段（ClassTypeSig+TypeVarSig 均可）。
- **深结构递归**（`testDeepStructure`）：`Map<String,int[]>` 双实参分型；`LOuter<TT;>.Inner;` outerType 也是 RefTypeSig；跨类型不等。
- **真类闭环**（`testRealClassGenericLoop`）：javac `TT;` 参数/返回 → RefTypeSig（精确 TypeVarSig）+ List&lt;String&gt; 实参 bound 也是 RefTypeSig。
