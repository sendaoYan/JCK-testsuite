# java.lang.classfile.Signature — 设计案

## 1. API 功能
`java.lang.classfile.Signature`（interface）。工厂/静态方法：parseFrom, of。查询/实例方法：signatureString。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `SignatureTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`SignatureTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature")` + public/interface/继承 + 关键成员。黑盒（`SignatureTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，11 → 161 断言）
- **round-trip 幂等表**（`testRoundTripIdempotent`）：22 个合法串（8 基类型 + V、类类型、数组、泛型、TypeVar、无界/有界通配、内部类点形式 `LOuter<TT;>.Inner;`、数组套泛型）逐个 `parseFrom(s).signatureString().equals(s)` + 二次解析幂等；instanceof 分型 12 断言。
- **of(ClassDesc) 工厂矩阵**（`testFactoryOf`）：String→ClassTypeSig、int→BaseTypeSig('I')、**void→"V" 合法**、`[I`/`[Ljava/lang/String;` 描述符→ArrayTypeSig、跨工厂值 equals、of(null)→NPE。
- **畸形串精确 IAE 表**（`testMalformedMatrix`）：17 串 ×（异常类型 + 消息片段）+ parseFrom(null)→NPE。实测锁定："bad("/"<"/"("/"nope"/"X"/"*"/"+…"/";" → `Unexpected character … at position 0, expected a type signature`；"I["/"["/"…List<*"/尾随垃圾 → `Not a valid type signature`；截断 → `Unexpected end of signature at position N, expected . or ;`；**"V" 可解析为 BaseTypeSig（签名解析层面不排除）**。
- **equals/hashCode/toString**（`testEqualityContract`）：跨 parse 值等、hashCode 一致、不同型不等、toString 非空。
- **真类泛型闭环**（`testRealClassGenericLoop`）：javac 产物 `gen` 方法 Signature 属性 == `<T:Ljava/lang/Object;>(TT;Ljava/util/List<Ljava/lang/String;>;)TT;`、`GENERIC_FIELD` == `Ljava/util/List<Ljava/lang/String;>;` → 模型回读幂等 + className。
- **build 注入闭环**（`testBuildAttributeLoop`）：`SignatureAttribute.of(Signature)` 注入字段 → re-parse `asTypeSignature()` 与原模型 equals；`of(Utf8Entry)` 双通道；`of((Signature)null)`→NPE。
