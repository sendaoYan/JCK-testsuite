# java.lang.classfile.TypeAnnotation — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation`（interface）。工厂/静态方法：of。查询/实例方法：targetInfo, targetPath, annotation。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TypeAnnotationTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypeAnnotationTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation")` + public/interface/继承 + 关键成员。黑盒（`TypeAnnotationTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，11 → 153 断言）
- `testFactoryNulls()`：`of(TargetInfo, List<TypePathComponent>, Annotation)` 回读三访问器 + annotation 元素值 + 两步 path + `targetInfo()/targetPath()/annotation()` 返回集合 add 全 UOE + 三个参数各自为 null 的 NPE（targetPath 为 null 时消息含 "coll"）。
- `testRealJavacProducts()`：真 javac 产物闭环——嵌套 `Holder`（`@TA` RUNTIME / `@TB` CLASS retention 两注解）经 `getResourceAsStream` 解析：类级（CLASS_EXTENDS supertypeIndex 65535/父类、0/1 接口）；字段 6 形态（`@A String`→path=[]、`List<@A String>`→[TYPE_ARGUMENT 0]、`List<? extends @A Number>`→[TYPE_ARGUMENT 0, WILDCARD]、双维数组 `String @A [] @A []`→[]+[ARRAY]、`List<@A String[]>`→[TYPE_ARGUMENT 0, ARRAY]、`@TB` 字段→RuntimeInvisibleTypeAnnotations）；方法级（泛型类/方法参数、返回值嵌套 path、throws）；代码级 11 条（CAST 普通/泛型锚 CHECKCAST bci、INSTANCEOF/NEW 锚指令 bci、METHOD_INVOCATION_TYPE_ARGUMENT、METHOD_REFERENCE 锚 INVOKEDYNAMIC bci、LOCAL_VARIABLE、泛型局部声明、multi-catch EXCEPTION_PARAMETER excIdx 0/1、RESOURCE_VARIABLE）。
- `testBuildInjection()`：字段级（`withField` 3 参重载）与代码级（`cob.newLabel()`+`labelBinding`）注入 RuntimeVisibleTypeAnnotations/RuntimeInvisibleTypeAnnotations → re-parse 回读，`attributeName()=="RuntimeVisibleTypeAnnotations"==Attributes.NAME_RUNTIME_VISIBLE_TYPE_ANNOTATIONS`、`attributeMapper()` 与注册表 `Attributes.runtimeVisibleTypeAnnotations()` 身份相等，varargs `of(TypeAnnotation...)`，verify 通过。
- 实测锁定：泛型局部变量声明（`List<@A String> x = ...`）同时产生 LOCAL_VARIABLE（声明位）与 CAST（右侧造型）两条注解；`@Retention(CLASS)` 注解落到 runtimeInvisibleTypeAnnotations；one 值 `findOne` 按 value 唯一锁定，对 javac 输出顺序鲁棒。

