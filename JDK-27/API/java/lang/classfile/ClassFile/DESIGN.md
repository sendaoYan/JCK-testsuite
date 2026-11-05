# java.lang.classfile.ClassFile — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile`（interface，JDK 24 正式、JDK 8 无此包）。工厂/静态：`of()`、`of(Option...)`、`latestMajorVersion()`、`latestMinorVersion()`；版本常量 `JAVA_1_VERSION..JAVA_27_VERSION`（45..71）、`MAGIC_NUMBER=0xCAFEBABE`、`PREVIEW_MINOR_VERSION=0xFFFF`、ACC_* 全套标志。实例：`withOptions`、`parse(byte[]/Path)`、`build`/`buildTo`/`buildModule`/`buildModuleTo`、`transformClass`、`verify(model/bytes/Path)`（List&lt;VerifyError&gt;）。嵌套 Option 类型 6 个：DebugElementsOption{PASS_DEBUG,DROP_DEBUG}、LineNumbersOption{PASS_LINE_NUMBERS,DROP_LINE_NUMBERS}、StackMapsOption{STACK_MAPS_WHEN_REQUIRED,GENERATE_STACK_MAPS,DROP_STACK_MAPS}、AttributeMapperOption、AttributesProcessingOption{PASS_ALL,DROP_UNKNOWN,DROP_UNSTABLE}、DeadLabelsOption{FAIL_ON_DEAD_LABELS,DROP_DEAD_LABELS}。

## 2. Java 用户如何使用
`ClassFile.of()` 得单例（== 幂等）；`build(ClassDesc, ClassBuilder 回调)` 从零生成字节码（内部自动做栈校验与 StackMapTable 生成，栈形状错抛 IAE "Operand stack underflow"）；`parse(byte[])` 得 ClassModel（错误 bytes 抛 IllegalArgumentException——空/坏魔数 "Bad magic number"、截断 "Reading beyond classfile bounds"，null 抛 NPE）；`transformClass(model, ClassTransform)` 管道改写；`verify(...)` 返回 VerifyError 列表。选项经 `withOptions(...)` 链式派生新实例。

## 3. 参考来源
- JDK 27 javadoc（java.lang.classfile 包，JDK 24 起 non-preview）。
- OpenJDK `test/jdk/jdk/classfile/`：OptionsTest（StrangeAttribute/DROP_UNKNOWN）、ClassBuildingTest、ModuleBuilderTest、SignaturesTest、Utf8EntryTest、AttributesTest、ShortJumpsFixTest。
- 本机实测探针（JDK-27.0.1）：异常消息、选项语义、Opcode 213、版本表、defineClass 闭环全部锁定。

## 4. 测试用例设计 + 易错规范细节
- 白盒 `testWhiteBox`：类加载/public/interface/关键成员声明。
- 黑盒 `testBlackBox`：常量精确值、build→magic 检验→parse 往返、verify 干净、withOptions 新实例。
- `testParseNegative`：null→NPE（消息含 classfileBytes）；空数组/垃圾/魔数截断/魔数错一位→IAE "Bad magic number"；正确魔数截断→IAE "Reading beyond classfile bounds"；build 栈下溢→IAE "Operand stack underflow"。**易错：错误 bytes 抛 IllegalArgumentException 而非 ClassFileException（实测锁定）**。
- `testVersionTable`：反射枚举全部 JAVA_x_VERSION 常量集合 == {45..71}；ClassFileVersion.of 回读；`cb.with(ClassFileVersion.of(52,0))` 注入→re-parse major==52（ClassFileVersion 是 ClassElement）。
- `testDefineClassLoop`：动态名（同包唯一后缀）build→`MethodHandles.lookup().defineClass(bytes)`→`getMethod("go").invoke(null)==1`、`add(20,22)==42`、静态字段 getstatic 默认 0——**生成类真运行闭环**。
- `testOptionSemantics`：发射 localVariable/lineNumber 伪指令后按选项 parse 计数矩阵——DROP_DEBUG 剥 LocalVariable 保留 LineNumber；DROP_LINE_NUMBERS 反之；属性两选项均保留；DROP_STACK_MAPS **build 侧**省略 SMT、parse 侧模型保留、transform 拷贝保留、无 SMT 的分支类 verify→VerifyError 非空；枚举 values() 数量表；withOptions 链式新实例。
- `testTransformPipeline`：ACCEPT_ALL 元素数守恒；dropping(e->true) 清空成员但保留 superclass；endHandler 注入字段计数+1；andThen 组合语义；transformingMethodBodies 自定义 CodeTransform 剥 NOP 后序列 [RETURN]。
- `testPathModuleVerify`：buildTo→Files.size>0→parse(Path)/verify(Path)；buildModule→isModuleInfo/ACC_MODULE/moduleName/moduleVersion 往返/verify 干净；临时目录独立 nanoTime 命名并 finally 清理。
- 输出 `PASS (N assertions)`，check 抛 AssertionError。
