# java.lang.classfile.constantpool.ConstantValueEntry — 设计案

## 1. API 功能
`java.lang.classfile.constantpool.ConstantValueEntry`（interface）。工厂/静态方法：(none)。查询/实例方法：constantValue。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ConstantValueEntryTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.constantpool.ConstantValueEntry` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ConstantValueEntryTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.constantpool.ConstantValueEntry")` + public/interface/继承 + 关键成员。黑盒（`ConstantValueEntryTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05）：ConstantValueEntry 契约
- 实现集矩阵（实测）：IntegerEntry/LongEntry/FloatEntry/DoubleEntry/StringEntry 是 ConstantValueEntry（且都 Loadable）；ClassEntry/MethodHandleEntry/MethodTypeEntry 是 Loadable 但**非** ConstantValueEntry。
- constantValue() 五类型精确回读（Integer/Long/Float/Double/String）。
- constantValueEntry(ConstantDesc) 分派：五常量类型→对应 Entry 同实例；ClassDesc/MethodTypeDesc → IAE("Illegal type")；null → NPE。
- 真类池闭环：static final 五常量字段经 ConstantValueAttribute.constant()（ConstantValueEntry 面）值全等回读（CONST_INT/LONG/FLOAT/DOUBLE/STRING）。
### 深化通用要点（2026-11-05，全部实测锁定）
- 去重契约：同值 put 两次返回**同一实例**（==）且 index() 相同；不同值不同 index。
- 发射闭环三段：build 发射指令 → re-parse 常量池含对应条目 → 访问器值回读。
- JDK-27 类初始化环：`MethodTypeDesc.ofDescriptor("(I)V")` 若是程序首个 AMHC 触点会触发 ConstantDescs↔PrimitiveClassDescImpl 初始化环（ExceptionInInitializerError）；本测试以 testConstantDescsPreinit()（先引用 ConstantDescs.CD_Object）规避，新增方法必须在 testBlackBox 之后运行。
- javac 产物真类池断言用测试类自带成员（static final 常量 / lambda 字段）保证条目存在。
