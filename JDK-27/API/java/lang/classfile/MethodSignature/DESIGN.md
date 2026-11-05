# java.lang.classfile.MethodSignature — 设计案

## 1. API 功能
`java.lang.classfile.MethodSignature`（interface）。工厂/静态方法：of, parseFrom。查询/实例方法：typeParameters, arguments, result, throwableSignatures, signatureString。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `MethodSignatureTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.MethodSignature` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`MethodSignatureTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.MethodSignature")` + public/interface/继承 + 关键成员。黑盒（`MethodSignatureTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，14 → 142 断言）
- **round-trip 幂等族**（`testRoundTripFamily`）：11 个合法方法签名（`(I)V`、3 参混合返回泛型、`<T:…>(TT;)TT;`、`^` throws 段类类型/TypeVar、`<T:>(I)V`、8 基类型参数、双 type param + throws TypeVar）全 round-trip + 二次幂等。
- **结构矩阵**（`testStructureMatrix`）：`(I)V` 空 tp/1 arg/V result；3 参签各 arg 分型（ClassTypeSig/BaseTypeSig/ArrayTypeSig）+ result 泛型下钻；`<T:…>` tp 逐访问器（identifier/classBound/interfaceBounds）；throws 段单/TypeVar/多段有序；result 三分型（Base/Class/TypeVar）；`(IJZDBCFS)V` 8 参数逐位 baseType。
- **畸形 IAE 表**（`testMalformedMatrix`）：实测锁定 "nope"/"I"/"bad("/"V" → `expected (`；"("/")"/"(I)" → `Not a valid method signature`；"(I)X" → `Unexpected character X at position 3`；"(I)TT" → `Unexpected end of signature at position 5`；parseFrom(null)→NPE。
- **工厂全重载**（`testFactoryMatrix`）：of(MethodTypeDesc)（含数组参数）、of(result, args…)、of(tps, throws, result, args…)（实测渲染 `<T:>(I)V` 并 re-parse equals）、多 interface bound 渲染 `<E::Ljava/lang/Runnable;>()V`；三个 null 负例 NPE。
- **build 注入闭环**（`testBuildAttributeLoop`）：`SignatureAttribute.of(MethodSignature)` → re-parse `asMethodSignature()` 与原模型 equals。
- **真类闭环**（`testRealClassGenericLoop`）：javac 产物 `gen` == `<T:Ljava/lang/Object;>(TT;Ljava/util/List<Ljava/lang/String;>;)TT;`、`genThrows` == `<T:Ljava/lang/Exception;>(TT;)V^TT;` → 模型逐访问器。
- **equals/hashCode**（`testEqualityContract`）：值等 + 差异（result/args）不等 + of(MTD)==parseFrom。
