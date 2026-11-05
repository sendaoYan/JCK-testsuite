# java.lang.classfile.TypeAnnotation.OffsetTarget — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.OffsetTarget`（interface）。工厂/静态方法：(none)。查询/实例方法：target。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `OffsetTargetTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.OffsetTarget` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`OffsetTargetTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$OffsetTarget")` + public/interface/继承 + 关键成员。黑盒（`OffsetTargetTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，9 → 55 断言）
- 工厂矩阵：4 个命名工厂 `ofInstanceofExpr/ofNewExpr/ofConstructorReference/ofMethodReference(Label)` + 通用 `ofOffset(TargetType,Label)`（INSTANCEOF/NEW/CONSTRUCTOR_REFERENCE/METHOD_REFERENCE 四形态）；`target()` Label 同一性、`size()==2`、`targetType()` 精确。
- 负例：`ofCastExpr` 等错型 → IAE；`ofOffset(FIELD, null)` IAE（**targetType 校验先于 Label 判空**）、`ofNewExpr(null)` NPE。
- 真 javac 产物锚点：`o instanceof @A String` 锚定 INSTANCEOF 指令 bci、`new @A Base()` 锚定 NEW 指令 bci、`@A String::isEmpty` 锚定 INVOKEDYNAMIC（lambda 引导）bci——全部用 `CodeAttribute.labelToBci(target())` 与 elementList 累计 sizeInBytes 求得的第 N 条指令偏移精确相等断言；注入 `cob.with(attr)` 代码级闭环。

