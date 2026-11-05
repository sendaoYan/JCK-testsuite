# java.lang.classfile.Signature.ThrowableSig — 设计案

## 1. API 功能
`java.lang.classfile.Signature.ThrowableSig`（interface）。工厂/静态方法：(none)。查询/实例方法：(none)。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ThrowableSigTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.ThrowableSig` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ThrowableSigTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$ThrowableSig")` + public/interface/继承 + 关键成员。黑盒（`ThrowableSigTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，7 → 56 断言）
- **标记接口白盒**（`testMarkerInterfaceShape`）：实测独立接口（不是 ClassTypeSig 的别名）、无自有方法、直接 extends Signature；**实现者矩阵：只有 ClassTypeSig 与 TypeVarSig，ArrayTypeSig/BaseTypeSig/抽象 RefTypeSig 均不可赋值**。
- **实现者矩阵**（`testImplementationMatrix`）：parseFrom 通道 `L…;`/`TT;` 是、`[…`/`I` 否；工厂通道双确认。
- **throws 段全形态**（`testThrowsSegmentMatrix`）：类类型/TypeVar/多段有序/泛型类 throwable 保留 typeArgs；5 串 round-trip 幂等（含 `^TT;` 与混合 `^…;^TT;`）。
- **build 注入闭环**（`testBuildAttributeLoop`）：asMethodSignature().throwableSignatures() 与模型 equals。
- **真类闭环**（`testRealClassGenericLoop`）：**javac `<T extends Exception> … throws T` → `<T:Ljava/lang/Exception;>(TT;)V^TT;`** → throws 段 TypeVarSig + ThrowableSig。
- **equals/hashCode**（`testEqualityContract`）。
