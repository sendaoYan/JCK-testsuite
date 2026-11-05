# java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget`（interface）。工厂/静态方法：(none)。查询/实例方法：typeParameterIndex, boundIndex。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TypeParameterBoundTargetTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypeParameterBoundTargetTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$TypeParameterBoundTarget")` + public/interface/继承 + 关键成员。黑盒（`TypeParameterBoundTargetTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，10 → 37 断言）
- 工厂三入口：`ofClassTypeParameterBound/ofMethodTypeParameterBound(tpIdx,boundIdx)` + 通用 `ofTypeParameterBound(TargetType,..)`（CLASS/METHOD_TYPE_PARAMETER_BOUND 两形态）；`size()==2`。
- u1 负例：typeParameterIndex 与 boundIndex **各自独立校验**——`(-1,0)` 报 "type parameter index out of range of u1"、`(0,-1)` 报 "bound index out of range of u1"、`(0,300)` 同理；`(255,255)` 合法；错型 FIELD → IAE。
- 真 javac 产物：`<T, U extends @A Number & @A Runnable>` 实测两条 METHOD_TYPE_PARAMETER_BOUND，typeParameterIndex=1（第二个类型参数），boundIndex 0/1（首个 bound 即类 bound 计 0，接口 bound 计 1）；单 bound 方法对照 idx (1,0)；path 恒空。

