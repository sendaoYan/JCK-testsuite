# java.lang.classfile.ClassFileBuilder — 设计案

## 1. API 功能
`java.lang.classfile.ClassFileBuilder`（interface）。工厂/静态方法：(none)。查询/实例方法：accept, with, constantPool, transform。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassFileBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFileBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassFileBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFileBuilder")` + public/interface/继承 + 关键成员。黑盒（`ClassFileBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，29 断言）

- `testConstantPoolChannel()`：constantPool() 通道——CodeTransform.accept 内 cb.constantPool().intEntry(12345) 预留条目 → 方法体 ldc 引用同条目 → re-parse 回读 12345（注入与引用共享池）；utf8Entry 去重 ==；entryByIndex(idx) 身份等同；越界索引抛 ConstantPoolException；MethodBuilder/FieldBuilder 的 constantPool() instanceof ConstantPoolBuilder（全树同一池）。
- `testWithAcceptTransform()`：accept(元素) 与 with(元素) 等效；with 链式返回自身；transform(ClassModel, ACCEPT_ALL) 整模型拷贝 verify 干净；CodeBuilder.transforming 语义实测——只过滤 handler 内发射的元素，transforming 之前已发射的 NOP 保留。
- `testTypeFaceAndDefineClass()`：DynCFB go()=="cfb" 运行闭环。

实测锁定契约事实：
1. 构建器树（ClassBuilder/MethodBuilder/FieldBuilder/CodeBuilder）的 constantPool() 返回同一 ConstantPoolBuilder 实例，可在任意层注入条目供其它层引用。
2. CodeBuilder.transforming 只拦截 transform 之后 handler 发射的指令流，不回溯改写之前已发射内容。
3. transform(ClassModel, ...) 可作整模型拷贝通道（ACCEPT_ALL 保真）；Interfaces 等非 CompoundElement 的小元素不能走 transform(CompoundElement) 通道，只能 with/accept。
4. entryByIndex 越界为 ConstantPoolException（非数组越界）。
