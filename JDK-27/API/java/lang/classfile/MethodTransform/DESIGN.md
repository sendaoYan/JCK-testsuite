# java.lang.classfile.MethodTransform — 设计案

## 1. API 功能
`java.lang.classfile.MethodTransform`（interface）。工厂/静态方法：ofStateful, endHandler, dropping, transformingCode。查询/实例方法：andThen。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `MethodTransformTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.MethodTransform` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`MethodTransformTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.MethodTransform")` + public/interface/继承 + 关键成员。黑盒（`MethodTransformTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，11→34 断言）：方法级变换全契约
新增 testAcceptAll（ACCEPT_ALL 经 transformingMethods 使用：名称/描述符/flags/code/属性全守恒、单例身份、is-a ClassFileTransform）；testDroppingAndEndHandler（dropping 剥 Deprecated 属性/剥 CodeModel（方法保留体消失）、endHandler with(DeprecatedAttribute.of()) 注入→re-parse、dropping.andThen(endHandler) 双效（实测））；testTransformingCodeAndStateful（transformingCode(CodeTransform) 桥剥 NOP、ofStateful Supplier 每方法一次（实测 2→2）、ofStateful/endHandler null NPE）。易错点：dropping 作用在 MethodElement（属性/CodeModel）上，剥整个方法要在 ClassTransform 层用谓词。
