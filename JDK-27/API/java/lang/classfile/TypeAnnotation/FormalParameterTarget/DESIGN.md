# java.lang.classfile.TypeAnnotation.FormalParameterTarget — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.FormalParameterTarget`（interface）。工厂/静态方法：(none)。查询/实例方法：formalParameterIndex。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `FormalParameterTargetTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.FormalParameterTarget` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`FormalParameterTargetTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$FormalParameterTarget")` + public/interface/继承 + 关键成员。黑盒（`FormalParameterTargetTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，8 → 35 断言）
- u1 边界：`ofMethodFormalParameter(0/255)` 合法回读，`-1/300` IAE 消息 "formal parameter index out of range of u1: N"；`size()==1`。
- 真 javac 产物：`void m(@A String p, int q, @A long r)` 实测 formalParameterIndex 为 **0 与 2**（声明序而非槽位序，long/double 双槽不占两个 index，被跳过的 int q 无注解）。
- 泛型参数 path：`m(List<@A String> p)` → [TYPE_ARGUMENT 0]，与 FormalParameterTarget 正交。

