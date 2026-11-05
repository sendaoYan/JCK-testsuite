# java.lang.classfile.TypeAnnotation.EmptyTarget — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.EmptyTarget`（interface）。工厂/静态方法：(none)。查询/实例方法：(none)。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `EmptyTargetTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.EmptyTarget` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`EmptyTargetTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$EmptyTarget")` + public/interface/继承 + 关键成员。黑盒（`EmptyTargetTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，6 → 48 断言）
- `of(TargetType)` 精确收窄矩阵：只接受 FIELD/METHOD_RETURN/METHOD_RECEIVER 三种（size()==0 的 target_info 空负载形态）；其余 19 种 TargetType 循环逐一断言 IAE（消息 "Wrong target type specified X"）。
- 真 javac 产物：`@A String f` 字段 → FIELD EmptyTarget；`@A List<String> m()` 返回值 → METHOD_RETURN EmptyTarget；path 恒空。
- 注入闭环：三种合法 target 全部经 `withField`/`withMethod` 注入 RuntimeVisibleTypeAnnotations → re-parse 回读 targetType 精确、verify 通过。

