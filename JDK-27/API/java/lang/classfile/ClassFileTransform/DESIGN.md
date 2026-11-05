# java.lang.classfile.ClassFileTransform — 设计案

## 1. API 功能
`java.lang.classfile.ClassFileTransform`（interface）。工厂/静态方法：(none)。查询/实例方法：accept, atEnd, atStart, andThen。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassFileTransformTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFileTransform` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassFileTransformTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFileTransform")` + public/interface/继承 + 关键成员。黑盒（`ClassFileTransformTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，10→37 断言）：根变换接口全契约
新增 testHierarchy（ClassTransform/CodeTransform/MethodTransform/FieldTransform 四接口 is-a ClassFileTransform、直接 extends 白盒、accept 抽象/atEnd/atStart 默认/andThen 抽象签名反射核对——transformClass 只收 ClassTransform，自造"原始 ClassFileTransform"须以其子接口形态实现）；testRawTransformLifecycle（自造匿名 ClassTransform：atStart→每元素 accept→atEnd 次序（实测 accept 恰等于源元素数）、hook 注入元素落盘、verify 干净）；testDefaultsAndAndThen（默认 atStart/atEnd 为 no-op（无注入）、andThen 生命周期 t1.atEnd 先于 t2.atEnd（实测 [t1end,t2end]）、双侧效果按序落盘 [f,inj1,inj2]、三族 andThen 存在性）。
