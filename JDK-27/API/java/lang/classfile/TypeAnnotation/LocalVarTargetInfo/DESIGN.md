# java.lang.classfile.TypeAnnotation.LocalVarTargetInfo — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.LocalVarTargetInfo`（interface）。工厂/静态方法：of。查询/实例方法：startLabel, endLabel, index。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `LocalVarTargetInfoTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.LocalVarTargetInfo` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`LocalVarTargetInfoTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$LocalVarTargetInfo")` + public/interface/继承 + 关键成员。黑盒（`LocalVarTargetInfoTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，11 → 28 断言）
- 访问器实名锁定：`startLabel()/endLabel()/index()`（**Label 视图而非 startPc/length/tableIndex**，与 JVMS 的 u2 三元组一一对应，经 `CodeAttribute.labelToBci` 折算 bci）。
- 校验矩阵：`of(null,e,0)`/`of(s,null,0)` NPE；`of(s,e,-1)` IAE "Invalid slot index :-1"；slot 0/255 合法。
- 真 javac 产物：局部变量注解的 start/end Label 落在其声明作用域内、index 为真实槽位号；注入 2 行表（两对 Label）经 build → re-parse 精确回读 slot 与行数。

