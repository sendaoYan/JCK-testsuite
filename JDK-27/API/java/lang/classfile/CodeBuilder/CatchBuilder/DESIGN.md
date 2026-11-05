# java.lang.classfile.CodeBuilder.CatchBuilder — 设计案

## 1. API 功能
`java.lang.classfile.CodeBuilder.CatchBuilder`（interface）。工厂/静态方法：(none)。查询/实例方法：catching, catchingMulti, catchingAll。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `CatchBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.CodeBuilder.CatchBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`CatchBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.CodeBuilder$CatchBuilder")` + public/interface/继承 + 关键成员。黑盒（`CatchBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，36 断言）

- `testCatchTableMatrix()`：catch 表精确矩阵——catching(IOException) + catchingMulti([ISE,UOE]) + catchingAll → 异常表 4 条，顺序即注册顺序；catchingMulti 展开的多条目共享同一 handler 标签与同一 try 区间（== 断言）；catchType 按序 java/io/IOException、java/lang/IllegalStateException、java/lang/UnsupportedOperationException、catchAll 为 empty Optional；链式双 catching → 顺序保持且 handler 标签互异。注：方法族是 catching/catchingMulti/catchingAll，无 catch_（实测锁定）。
- `testCatchRuntime()`：defineClass 运行闭环——除零被 catching(ArithmeticException) 捕获 → 42；aconst_null+athrow 被 catchingAll 捕获 → 7；ISE 不被 AE handler 捕获（异常表类型精确匹配，穿透传播 InvocationTargetException(cause=ISE)）。
- `testCatchRethrow()`：catch 块内 athrow 直接重抛（cause==NPE）；handler 入口栈顶即异常对象——invokevirtual(Object.toString) 返回以 "java.lang.NullPointerException" 开头的串。

实测锁定契约事实：
1. catchingMulti(List) 每个类型生成一条独立异常表条目，但 handler/tryStart/tryEnd 标签三相同（共享处理块）。
2. catchingAll 等价 catchType 为 empty Optional（catch_type=0），表序保持注册顺序，与 JVM 匹配顺序（首个匹配生效）一致。
3. 异常表匹配是类型精确的：handler 声明 AE 时 ISE 不被捕获（非子类穿透）。
4. 进入 catch handler 时操作数栈只有异常对象（栈顶），可直接 invokevirtual 消费。
