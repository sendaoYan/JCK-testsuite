# java.lang.classfile.TypeAnnotation.TargetInfo — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.TargetInfo`（interface）。工厂/静态方法：ofTypeParameter, ofClassTypeParameter, ofMethodTypeParameter, ofClassExtends, ofTypeParameterBound, ofClassTypeParameterBound, ofMethodTypeParameterBound, of。查询/实例方法：targetType, size。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TargetInfoTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.TargetInfo` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TargetInfoTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$TargetInfo")` + public/interface/继承 + 关键成员。黑盒（`TargetInfoTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，13 → 99 断言）
- `testFactoryMatrix()`：全部工厂（ofTypeParameter/ofClassTypeParameter/ofMethodTypeParameter/ofClassExtends/ofTypeParameterBound×2/ofMethodTypeParameterBound/of(TargetType)/ofField/ofMethodReturn/ofMethodReceiver/ofMethodFormalParameter/ofThrows/ofVariable/ofLocalVariable/ofResourceVariable/ofExceptionParameter/ofOffset/ofInstanceofExpr/ofNewExpr/ofConstructorReference/ofMethodReference/ofTypeArgument/ofCastExpr/ofConstructorInvocationTypeArgument/ofMethodInvocationTypeArgument/ofConstructorReferenceTypeArgument/ofMethodReferenceTypeArgument）逐一回读 targetType + 专属访问器 + `size()`；实测 size 表：CTP/MTP=1、CLASS_EXTENDS/CTPB/MTPB/THROWS/EXCEPTION_PARAMETER/INSTANCEOF/NEW/CTOR_REF/METHOD_REF=2、FIELD/METHOD_RETURN/METHOD_RECEIVER=0、MFP=1、CAST/各 TYPE_ARGUMENT=3、LocalVar=2+6×tableSize（空表=2）。
- `testValidation()`：`of(null)` NPE（消息含 targetType）；`of(CAST/CLASS_TYPE_PARAMETER/THROWS/LOCAL_VARIABLE/METHOD_FORMAL_PARAMETER/NEW)` IAE "Wrong target type specified X"；`ofTypeParameter(FIELD,..)`/`ofVariable(FIELD,..)`/`ofOffset(FIELD,null)` IAE（类型校验先于 Label 判空）；`ofClassExtends(-1/65536)`、`ofThrows`、`ofExceptionParameter` u2 负例消息 "… out of range of u2: N"；`ofMethodFormalParameter(-1/300)` u1（255 合法）；`ofClassTypeParameterBound(-1,0)/(0,300)`；`LocalVarTargetInfo.of(null,e,0)/(s,null,0)` NPE、`of(s,e,-1)` IAE "Invalid slot index :-1"。
- `testLabelFactoriesAndProduct()`：`ClassFile.of().build()` 回调内 9 个 Label 形态工厂（CAST/INSTANCEOF/NEW/双 REFERENCE/4×TYPE_ARGUMENT）Label 同一性；真 javac 产物 `@TI` 字段 → ofField 形态 EmptyTarget。

