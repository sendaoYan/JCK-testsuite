# java.lang.classfile.FieldTransform — 设计案

## 1. API 功能
`java.lang.classfile.FieldTransform`（interface）。工厂/静态方法：ofStateful, endHandler, dropping。查询/实例方法：andThen。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `FieldTransformTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.FieldTransform` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`FieldTransformTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.FieldTransform")` + public/interface/继承 + 关键成员。黑盒（`FieldTransformTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，11→32 断言）：字段级变换全契约
新增 testAcceptAll（ACCEPT_ALL 经 transformingFields：名称/类型/flags/ConstantValue/Deprecated 全守恒、单例身份）；testDroppingAndEndHandler（dropping 剥 ConstantValue/剥全部属性、endHandler with(SyntheticAttribute.of()) 注入→re-parse、dropping.andThen(endHandler) 双效）；testOfStateful（Supplier 每字段一次（实测 2 字段→2 次）、ofStateful/endHandler/dropping null NPE）。易错点：dropping 作用在 FieldElement（字段属性）上；剥整个字段在 ClassTransform 层。
