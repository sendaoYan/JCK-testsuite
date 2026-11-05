# java.lang.classfile.Signature.TypeParam — 设计案

## 1. API 功能
`java.lang.classfile.Signature.TypeParam`（interface）。工厂/静态方法：of。查询/实例方法：identifier, classBound, interfaceBounds。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TypeParamTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.TypeParam` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypeParamTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$TypeParam")` + public/interface/继承 + 关键成员。黑盒（`TypeParamTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，12 → 61 断言）
- **解析矩阵**（`testParseMatrix`）：classBound 存在（`<T:Ljava/lang/Object;>`）/缺失（`<T::…>`）/TypeVar（`<T:TT;>`）/无 bound（**`<T:>` 实测合法 quirk**）；多 interface bound `<E:…:…:…>` 有序；多类型参数 `<T:…;U::…;>` 顺序保持；MethodSignature 侧同语义。
- **工厂全重载**（`testFactoryMatrix`）：of(String, RefTypeSig, …)/of(String, Optional, …)；**实测 of("T", (RefTypeSig)null) → classBound 为 empty（合法）**；多 bound 工厂精确渲染 `<E:Ljava/lang/Object;:Ljava/lang/Runnable;:Ljava/io/Serializable;>Ljava/lang/Object;` + re-parse equals。
- **负例**（`testNegativeMatrix`）：of(null id) → NPE 消息含 `"st" is null`；**interfaceBounds() 不可变（add → UnsupportedOperationException）**。
- **equals/hashCode**（`testEqualityContract`）：工厂==解析值等。
- **真类闭环**（`testRealClassGenericLoop`）：**javac `<T extends Object, U extends Comparable<?>> void genTP(T,U)` → `<T:Ljava/lang/Object;U::Ljava/lang/Comparable<*>;>(TT;TU;)V`**，T/U 逐访问器 + U 的 ib 实参 Unbounded。
