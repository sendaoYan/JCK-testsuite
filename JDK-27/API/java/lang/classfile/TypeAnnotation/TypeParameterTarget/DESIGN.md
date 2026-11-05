# java.lang.classfile.TypeAnnotation.TypeParameterTarget — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.TypeParameterTarget`（interface）。工厂/静态方法：(none)。查询/实例方法：typeParameterIndex。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TypeParameterTargetTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.TypeParameterTarget` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypeParameterTargetTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$TypeParameterTarget")` + public/interface/继承 + 关键成员。黑盒（`TypeParameterTargetTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，8 → 33 断言）
- 工厂三入口：`ofClassTypeParameter/ofMethodTypeParameter(int)` + 通用 `ofTypeParameter(TargetType,int)`（CLASS/METHOD_TYPE_PARAMETER 两形态）；`size()==1`。
- u1 负例：`ofClassTypeParameter(-1)`/`ofMethodTypeParameter(300)` IAE 精确消息 "type parameter index out of range of u1: N"，255 合法；`ofTypeParameter(FIELD,0)` IAE（"Wrong target type"）。
- 真 javac 产物双位点：类级 `class GHolder<@A T, @A U>` 的类文件携带两条 CLASS_TYPE_PARAMETER（index 0/1 声明序，path 空）；方法级 `<@A T, @A U> void m` 携带两条 METHOD_TYPE_PARAMETER（index 0/1）——同一注解类型在两类 target 下形态正交。

