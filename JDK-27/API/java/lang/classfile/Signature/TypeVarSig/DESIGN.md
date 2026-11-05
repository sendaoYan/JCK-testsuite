# java.lang.classfile.Signature.TypeVarSig — 设计案

## 1. API 功能
`java.lang.classfile.Signature.TypeVarSig`（interface）。工厂/静态方法：of。查询/实例方法：identifier。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TypeVarSigTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.TypeVarSig` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypeVarSigTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$TypeVarSig")` + public/interface/继承 + 关键成员。黑盒（`TypeVarSigTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，9 → 69 断言）
- **五个语法位置全矩阵**（`testParsePositions`）：参数位/返回位（`(TT;)TT;`）、throws 位（`()V^TT;`）、类型实参位（`Map<TK;TV;>` 双标识符）、type param classBound 位（`<T:TT;>`）、数组元素位（`[TT;`）+ 有界通配 bound 位（`<+TT;>`）。
- **of 工厂矩阵**（`testFactoryMatrix`）：of("X")→`TX;`、of("Element")→`TElement;`；**实测 of("") → IAE `Not a valid identifier`**、of(null) → NPE 消息含 `"st" is null`。
- **畸形 IAE 表**（`testMalformedMatrix`）：实测 "T;" → `Unexpected character ; at position 1, expected an identifier`；"TT" → `Unexpected end … expected ;`；"T" → `Unexpected end … expected an identifier`；parseFrom(null)→NPE。
- **类型层次**（`testSubtypingMatrix`）：RefTypeSig + ThrowableSig 双身份；6 串 round-trip 幂等。
- **equals/hashCode**（`testEqualityContract`）。
- **真类闭环**（`testRealClassGenericLoop`）：javac `TT;` 参数/返回 → TypeVarSig identifier=="T"。
