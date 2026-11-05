# java.lang.classfile.attribute.SignatureAttribute — 设计案

## 1. API 功能
`java.lang.classfile.attribute.SignatureAttribute`（interface）。工厂/静态方法：of。查询/实例方法：signature, asClassSignature, asMethodSignature, asTypeSignature。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `SignatureAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.SignatureAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`SignatureAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.SignatureAttribute")` + public/interface/继承 + 关键成员。黑盒（`SignatureAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化条目（2026-11-05 B 批，38 断言）
- testFactoryOverloads：of(ClassSignature)/of(MethodSignature)/of(Signature)/of(Utf8Entry) 4 重载（Utf8 身份保留）；NAME_SIGNATURE/CP_REFS；of(null) NPE。
- testAsViews：asClassSignature/asMethodSignature/asTypeSignature 正向 + **错型三方向均 IAE**（实测）。
- testRealProduct：javac 三处精确串——类 `<T::Ljava/io/Serializable;>Ljava/lang/Object;`、字段 `Ljava/util/List<Ljava/lang/String;>;`、方法 `<T::Ljava/util/List<Ljava/lang/String;>;>(Ljava/util/List<TT;>;)TT;`（接口 bound 为空类 bound 双冒号）。
- testInjectRoundTrip：三级注入回读 + 串幂等 re-parseFrom + verify 干净；result() 用 signatureString()（toString 是 impl dump——实测）。
