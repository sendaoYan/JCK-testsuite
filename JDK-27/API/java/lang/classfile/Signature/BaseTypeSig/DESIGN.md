# java.lang.classfile.Signature.BaseTypeSig — 设计案

## 1. API 功能
`java.lang.classfile.Signature.BaseTypeSig`（interface）。工厂/静态方法：of。查询/实例方法：baseType。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `BaseTypeSigTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.BaseTypeSig` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`BaseTypeSigTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$BaseTypeSig")` + public/interface/继承 + 关键成员。黑盒（`BaseTypeSigTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，10 → 119 断言）
- **9 基类型字符全表**（`testBaseBattery`）：I/J/Z/D/B/C/F/S/V × of(char)/of(ClassDesc)/parseFrom 三通道，baseType/signatureString/round-trip/值 equals 全断言。
- **方法签名 8 参数逐位矩阵**（`testMethodArgMatrix`）：`(IJZDBCFS)V` 每位 instanceof + baseType 精确；**V 只在 result 位出现，但 Signature.parseFrom("V") 与 of(CD_void) 均合法（实测 quirk）**。
- **非法负例**（`testInvalidMatrix`）：of('X'/'A'/'x'/'0'/(char)0) → IAE `invalid base type signature`；of(CD_String) → IAE `primitive class type required`；of((ClassDesc)null)→NPE；parseFrom("X")/"I[" IAE 消息。
- **类型层次**（`testSubtypingMatrix`）：是 Signature，**不是 RefTypeSig/ThrowableSig**；可作数组元素位。
- **真类闭环**（`testRealClassLoop`）：javac `baseGen` == `(IJ)Ljava/util/List<Ljava/lang/String;>;` → 参数位 BaseTypeSig 逐位。
- **build 注入闭环**（`testBuildAttributeLoop`）+ **equals/hashCode**（不同基字符不等）。
