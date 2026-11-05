# java.lang.classfile.TypeAnnotation.TypeArgumentTarget — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.TypeArgumentTarget`（interface）。工厂/静态方法：(none)。查询/实例方法：target, typeArgumentIndex。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TypeArgumentTargetTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.TypeArgumentTarget` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypeArgumentTargetTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$TypeArgumentTarget")` + public/interface/继承 + 关键成员。黑盒（`TypeArgumentTargetTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，11 → 74 断言）
- 工厂矩阵：6 个命名工厂 `ofCastExpr/ofConstructorInvocationTypeArgument/ofMethodInvocationTypeArgument/ofConstructorReferenceTypeArgument/ofMethodReferenceTypeArgument(Label,int)` + 通用 `ofTypeArgument(TargetType,Label,int)` 覆盖全部 5 种形态（CAST/CONSTRUCTOR_INVOCATION/METHOD_INVOCATION/CONSTRUCTOR_REFERENCE/METHOD_REFERENCE ×TYPE_ARGUMENT）；`typeArgumentIndex()/target()` Label 同一性、`size()==3`。
- 负例：`ofCastExpr(null,0)`、`ofMethodInvocationTypeArgument(null,0)` NPE；`ofTypeArgument(FIELD,..)`/`ofTypeArgument(INSTANCEOF,..)` IAE（消息含 "Wrong target type"）。
- 真 javac 产物：`(@A String) o` → CAST taIdx=0 path=[] 锚第 1 条 CHECKCAST bci；`(List<@A String>) o` → CAST path=[TYPE_ARGUMENT 0] 锚第 2 条 CHECKCAST；`Collections.<@A String>singleton(x)` → METHOD_INVOCATION_TYPE_ARGUMENT（锚点跟随实参加载处，只断言 label 在范围内，不断言精确 bci）；泛型局部声明 `List<@A String> gcs = (List<@A String>) ...` 实测同时产生 LOCAL_VARIABLE（声明位 value=gcta）与 CAST（造型位 value=gcast）两条注解——value 命名分离以保持 findOne 唯一性。

