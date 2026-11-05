# java.lang.classfile.MethodBuilder — 设计案

## 1. API 功能
`java.lang.classfile.MethodBuilder`（interface）。工厂/静态方法：(none)。查询/实例方法：withFlags, withCode, transformCode。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `MethodBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.MethodBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`MethodBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.MethodBuilder")` + public/interface/继承 + 关键成员。黑盒（`MethodBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，60 断言）

- `testAttributeMatrix()`：方法属性矩阵——ExceptionsAttribute 2 个异常按序回读 / DeprecatedAttribute / SignatureAttribute "(II)I" 精确串 / MethodParametersAttribute ofParameter("a",ACC_FINAL) 模板 + 未命名参数（name 为 empty） / withFlags(169) flagsMask 回读。
- `testAbstractNative()`：abstract(1025)/native(257) 无 code 合法且 verify 干净；toString 为 "AccessFlags[flags=1025]"；abstract+code 组合 build 放行、verify 拒绝；BRIDGE|SYNTHETIC(0x1040) 可写可读；ACC_STRICT(0x800, v71 已废弃) flagsMask 可回读但 flags() 抛 IAE "Unmatched bit position 0x800 for location METHOD"。
- `testNegativeMatrix()`：withCode(null) NPE "handler"；空 body IAE；同 builder 双 withCode 后者整体覆盖前者（ops==[ICONST_2,IRETURN]）。
- `testTransformChannel()`：mb.transformCode 剥 NOP / ACCEPT_ALL 保真 / MethodTransform 回调内 constantPool() 可用 / withCode(Utf8Entry 池条目重载)。
- `testDefineClassLoop()`：DynMB mul(6,7)==42、abs(-9)==9、abs(5)==5 运行闭环。

实测锁定契约事实：
1. 双 withCode 不是叠加而是整体覆盖（最后一次生效）。
2. abstract/native 方法携带 withCode → build 成功、verify 报错（与字段查重同为「结构放行、验证拒绝」分层）。
3. ACC_STRICT(0x800) 在 v71 已废弃：mask 通道可读写，flags() 查询通道抛 IAE 且消息含 "Unmatched bit position"。
4. AccessFlags.toString() 格式为 "AccessFlags[flags=N]"（十进制位掩码）。
