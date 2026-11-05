# java.lang.classfile.FieldBuilder — 设计案

## 1. API 功能
`java.lang.classfile.FieldBuilder`（interface）。工厂/静态方法：(none)。查询/实例方法：withFlags。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `FieldBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.FieldBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`FieldBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.FieldBuilder")` + public/interface/继承 + 关键成员。黑盒（`FieldBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，48 断言）

- `testConstantValueMatrix()`：ConstantValueAttribute.of 全类型矩阵——of(42)→IntegerEntry 42 / of(2L) / of(3.5f) / of(2.5d) / of("strv")→StringEntry，全部 re-parse 回读值相等；attributeName()=="ConstantValue"；mapper==Attributes.constantValue()；of(ConstantValueEntry) 条目重载（77）；of((ConstantDesc)null) 抛裸 NPE（getMessage()==null）。注：of(boolean)/of(char) 不存在（Boolean/Character 非 ConstantDesc）——实测锁定。
- `testFlagBits()`：VOLATILE|TRANSIENT(192) / ENUM(16400|PUBLIC=16408) 位掩码回读；flags().location()==AccessFlag.Location.FIELD；toString "AccessFlags[flags=192]"；withFlags(AccessFlag...) 变长重载。
- `testAttributeAndTransform()`：Signature 精确描述符串 / Deprecated / Synthetic 的 attributeName 表 + Utf8Entry 名重载；transformField(ACCEPT_ALL) 保真、dropping(Signature) 剥离；FieldTransform 回调内 constantPool() 非 null。
- `testDefineClassLoop()`：DynFB 常量字段 C==77（反射读 + getstatic 生成方法读双通道）。

实测锁定契约事实：
1. ConstantValueAttribute.of 只接受 int/long/float/double/String（ConstantDesc 语义）；boolean/char 无工厂。
2. 静态字段常量池注入在 build 层完成，re-parse 后 Entry 类型按值域精确分型（IntegerEntry/LongEntry/FloatEntry/DoubleEntry/StringEntry）。
3. 生成类的 static 字段若带 ACC_FINAL 且在生成 <clinit> 外 putstatic，运行时抛 IllegalAccessError（"Update to static final field"）——测试生成器避免对 final 字段写。
4. AccessFlag.Location.FIELD 与 METHOD 分域，flags() 按域校验非法位。
