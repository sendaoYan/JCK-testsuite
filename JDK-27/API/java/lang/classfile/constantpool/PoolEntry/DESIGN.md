# java.lang.classfile.constantpool.PoolEntry — 设计案

## 1. API 功能
`java.lang.classfile.constantpool.PoolEntry`（interface）。工厂/静态方法：(none)。查询/实例方法：constantPool, tag, index, width。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `PoolEntryTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.constantpool.PoolEntry` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`PoolEntryTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.constantpool.PoolEntry")` + public/interface/继承 + 关键成员。黑盒（`PoolEntryTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05）：PoolEntry 通用面契约
- TAG 常量全表（javap 实测）：UTF8=1/INTEGER=3/FLOAT=4/LONG=5/DOUBLE=6/CLASS=7/STRING=8/FIELDREF=9/METHODREF=10/INTERFACE_METHODREF=11/NAME_AND_TYPE=12/METHOD_HANDLE=15/METHOD_TYPE=16/DYNAMIC=17/INVOKE_DYNAMIC=18/MODULE=19/PACKAGE=20；反射枚举全 17 个无遗漏（2/13/14 为 JVMS 保留）。
- width 表：全部单槽除 LONG/DOUBLE=2；单槽条目 index 互异且 >=1；long 后条目 index 跳 1、double 后跳 2。
- **易错**：PoolEntry 是 width() 不是 length()；TAG 常量名是 TAG_INTERFACE_METHODREF/TAG_METHOD_HANDLE/TAG_METHOD_TYPE/TAG_NAME_AND_TYPE/TAG_INVOKE_DYNAMIC（非 INTERFACEMETHODREF 等连写）。
- toString 实测含 index 与内容（如 "10 java/lang/Object.<init>:()V"）。
- 真类池通律：每条目 tag 合法 + entryByIndex 反查身份 + constantPool()==池 + width 法则（5/6 tag→2 其余 1）；size()==maxIndex+1。
### 深化通用要点（2026-11-05，全部实测锁定）
- 去重契约：同值 put 两次返回**同一实例**（==）且 index() 相同；不同值不同 index。
- 发射闭环三段：build 发射指令 → re-parse 常量池含对应条目 → 访问器值回读。
- JDK-27 类初始化环：`MethodTypeDesc.ofDescriptor("(I)V")` 若是程序首个 AMHC 触点会触发 ConstantDescs↔PrimitiveClassDescImpl 初始化环（ExceptionInInitializerError）；本测试以 testConstantDescsPreinit()（先引用 ConstantDescs.CD_Object）规避，新增方法必须在 testBlackBox 之后运行。
- javac 产物真类池断言用测试类自带成员（static final 常量 / lambda 字段）保证条目存在。
