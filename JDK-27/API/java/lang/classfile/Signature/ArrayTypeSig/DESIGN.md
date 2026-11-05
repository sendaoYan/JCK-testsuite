# java.lang.classfile.Signature.ArrayTypeSig — 设计案

## 1. API 功能
`java.lang.classfile.Signature.ArrayTypeSig`（interface）。工厂/静态方法：of。查询/实例方法：componentSignature。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ArrayTypeSigTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.ArrayTypeSig` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ArrayTypeSigTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$ArrayTypeSig")` + public/interface/继承 + 关键成员。黑盒（`ArrayTypeSigTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，10 → 104 断言）
- **parseFrom 合法族**（`testParseFamily`）：8 基类型数组 + 引用数组 + 多维（`[[D`/`[[[Z`）+ 泛型元素 + TypeVar 元素，全 round-trip + 二次幂等 + instanceof ArrayTypeSig。
- **componentSignature 逐层下钻**（`testComponentWalk`）：`[[D` → `[D` → BaseTypeSig('D')；引用数组元素 ClassTypeSig.className；泛型元素 typeArgs 下钻（Unbounded）；TypeVar 元素 identifier。
- **of 工厂矩阵**（`testFactoryMatrix`）：of(sig) 深度 1、of(int,sig) 深度 2（comp 嵌套）、of(3, ref)；跨工厂值 equals（parseFrom==of）；**实测 of(0,·)/of(-1,·) → IAE `illegal array depth value`**；of(null)→NPE。
- **类型层次**（`testSubtypingMatrix`）：是 Signature/RefTypeSig，不是 ThrowableSig/ClassTypeSig；引用元素也是 RefTypeSig。
- **真类闭环**（`testRealClassFieldLoop`）：**实测锁定 javac 不为「签名==描述符」的纯数组字段发 Signature 属性**（ARRAY_FIELD/MATRIX_FIELD 断言 isEmpty）；泛型元素数组 `List<String>[]` → `[Ljava/util/List<Ljava/lang/String;>;` → 模型下钻。
- **build 注入闭环**（`testBuildAttributeLoop`）+ **equals/hashCode**（深度/元素差异不等）。
