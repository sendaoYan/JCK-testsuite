# java.lang.classfile.TypeAnnotation.CatchTarget — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.CatchTarget`（interface）。工厂/静态方法：(none)。查询/实例方法：exceptionTableIndex。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `CatchTargetTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.CatchTarget` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`CatchTargetTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$CatchTarget")` + public/interface/继承 + 关键成员。黑盒（`CatchTargetTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，8 → 43 断言）
- 工厂边界：`ofExceptionParameter(0/1/255/65535)` 回读 + `targetType()==EXCEPTION_PARAMETER` + `size()==2`；`-1/65536` IAE 消息 "exception table index out of range of u2: N"（65535 合法）。
- 真 javac 产物：multi-catch `catch (@A IOException | @A IllegalStateException e)` 实测产生**两条**注解，exceptionTableIndex 0 与 1（每个备选类型各一条，JVMS 4.7.20.1）；普通 catch 链后续 handler 依次 2、3；异常类用简单名（import java.io.IOException），限定名前注解会被 javac 拒绝。
- 访问器防御：`exceptionTableIndex()` 精确回读、instanceof TargetInfo 成立；`instanceof` 表达式经 `((Object) x)` 视图转换避免编译期类型冲突。

