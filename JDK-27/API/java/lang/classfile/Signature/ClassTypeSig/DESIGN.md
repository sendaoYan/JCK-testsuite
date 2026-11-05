# java.lang.classfile.Signature.ClassTypeSig — 设计案

## 1. API 功能
`java.lang.classfile.Signature.ClassTypeSig`（interface）。工厂/静态方法：of。查询/实例方法：outerType, className, classDesc, typeArgs。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassTypeSigTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Signature.ClassTypeSig` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassTypeSigTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Signature$ClassTypeSig")` + public/interface/继承 + 关键成员。黑盒（`ClassTypeSigTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，13 → 91 断言）
- **parseFrom 结构矩阵**（`testParseMatrix`）：plain/List&lt;String&gt;/Map&lt;TK;TV;&gt;/`LOuter<TT;>.Inner;` 内部类点形式/List&lt;*&gt;——className/outerType/typeArgs/classDesc 逐访问器；**实测 inner.classDesc().descriptorString()=="LOuter$Inner;"、displayName()=="Outer$Inner"**；6 串 round-trip 幂等。
- **of 工厂全 4 重载**（`testFactoryMatrix`）：of(CD)/of(String)/of(CD, args…)/of(String, args…)/嵌套 of(outer,"Inner")→`Lpkg/Outer.Inner;`（outer 实参保留 → `Lpkg/Outer<Ljava/lang/String;>.Inner<*>;`）+ deprecated of(outer, CD) 形态 + of==parseFrom 值 equals。
- **负例**（`testNegativeMatrix`）：**实测嵌套第二段必须是简单标识符：of(outer,"pkg/Outer$Inner") → IAE `Not a valid identifier`**；of((ClassDesc)null) NPE 消息含 `"cd" is null`；of((String)null) 含 `"st" is null`。
- **类型层次**（`testSubtypingMatrix`）：RefTypeSig + ThrowableSig 双身份，可直接作 throws 段成员。
- **真类闭环**（`testRealClassFieldLoop`）：javac 泛型字段 == `Ljava/util/List<Ljava/lang/String;>;` → className/typeArgs/classDesc。
- **build 注入闭环**（`testBuildAttributeLoop`）+ **equals/hashCode**（类名/实参差异不等）。
