# java.lang.classfile.ClassSignature — 设计案

## 1. API 功能
`java.lang.classfile.ClassSignature`（interface）。工厂/静态方法：of, parseFrom。查询/实例方法：typeParameters, superclassSignature, superinterfaceSignatures, signatureString。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassSignatureTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassSignature` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassSignatureTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassSignature")` + public/interface/继承 + 关键成员。黑盒（`ClassSignatureTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，15 → 112 断言）
- **round-trip 幂等族**（`testRoundTripFamily`）：9 个合法类签名（plain、`<T:…>`、`<T::…>…;Iface`、多 superinterface、**`<T:>…` 无 bound 合法 quirk**、多 interface bound `<E:…:…:…>`、`<T:TT;>` TypeVar classBound、泛型超类 `Ljava/util/AbstractMap<TK;TV;>;` + 泛型 iface）全 round-trip + 二次幂等。
- **结构矩阵**（`testStructureMatrix`）：TypeParam 逐访问器（classBound 存在/`::` 缺失/interfaceBounds）；多 iface 有序；`<T:TT;>` classBound TypeVarSig；泛型超类 typeArgs 下钻；superclass/iface 均 ClassTypeSig（同时是 RefTypeSig/ThrowableSig）。
- **畸形 IAE 表**（`testMalformedMatrix`）：实测锁定 "bad("/"I"/"(" → `expected L`；"" → `Unexpected end of signature at position 0, expected L`；"<" → `expected an identifier`；`<T:Ljava/lang/Object;>`（无超类）→ IAE；"Ljava/lang/Object"（无分号）→ `expected . or ;`；parseFrom(null)→NPE。
- **工厂全重载**（`testFactoryMatrix`）：of(cts)/of(cts, ifaces…)/of(tps, cts, ifaces…) + 多 interface bound 精确渲染 `<E:Ljava/lang/Object;:Ljava/lang/Runnable;:Ljava/io/Serializable;>Ljava/lang/Object;` + re-parse equals + of(null) NPE。
- **build 注入闭环**（`testBuildAttributeLoop`）：`SignatureAttribute.of(ClassSignature)` 注入类 → re-parse `asClassSignature()` equals。
- **真类闭环**（`testRealClassGenericLoop`）：javac 嵌套类 `CsBox` == `<T:Ljava/lang/Object;>Ljava/lang/Object;`、`CsPair` == `<K:Ljava/lang/Object;V::Ljava/lang/Comparable<TK;>;>Ljava/lang/Object;Ljava/lang/Runnable;` → 模型逐访问器。
- **equals/hashCode**（`testEqualityContract`）。
