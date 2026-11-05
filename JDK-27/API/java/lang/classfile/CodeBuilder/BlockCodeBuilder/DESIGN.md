# java.lang.classfile.CodeBuilder.BlockCodeBuilder — 设计案

## 1. API 功能
`java.lang.classfile.CodeBuilder.BlockCodeBuilder`（interface）。工厂/静态方法：(none)。查询/实例方法：breakLabel。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `BlockCodeBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.CodeBuilder.BlockCodeBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`BlockCodeBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.CodeBuilder$BlockCodeBuilder")` + public/interface/继承 + 关键成员。黑盒（`BlockCodeBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，29 断言）

- `testNestedBlocks()`：三层嵌套 block——内层 ifgt(breakLabel()) 跳出；外层块剩余代码成死代码，实测 re-parse 序列精确为 [ILOAD_0, IFGT, ICONST_1, IRETURN, NOP, ATHROW, NOP, ATHROW]；运行 m(-1)==1 / m(1)==1；三层 breakLabel() 全部非 null。注：BlockCodeBuilder 只声明 breakLabel()，无 continueLabel（实测锁定）。
- `testGotoOuterLabel()`：内层块 goto 到外层手建 label（newLabel+labelBinding），块与手建标签协作，运行返回 7，verify 干净。
- `testIfThenElse()`：ifThenElse(IFNE, then, else) 双分支均 ireturn 时只发 1 条条件跳转（无额外 goto——实测）；ifThen(IFLT) 单分支；运行 5/0/-3/3 四点断言。
- `testTryingRuntime()`：try 块内 breakLabel 跳出 try 区间 → 9；除零被 catch → 42；catch 块 handler 参数本身是 BlockCodeBuilder（h.breakLabel() 非 null，属于 catch 块自身）。

实测锁定契约事实：
1. block(l -> ...) 的 lambda 参数即 BlockCodeBuilder（继承 CodeBuilder 全部发射能力），breakLabel() 是本块出口标签；跳到它 = 结束本块继续块后代码。
2. 内层提前 return 后外层剩余发射成死代码 → NOP+ATHROW 填充（与 CodeBuilder 死代码语义一致）。
3. ifThenElse 双分支都终止时编译产物只有 1 条条件跳转；then 分支 fallthrough 场景才补 goto。
4. try 块（trying 的第一个参数）也是 BlockCodeBuilder，可 breakLabel 跳出 try 区间（异常表区间不覆盖后续代码）。
