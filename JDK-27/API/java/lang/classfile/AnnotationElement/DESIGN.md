# java.lang.classfile.AnnotationElement — 设计案

## 1. API 功能
`java.lang.classfile.AnnotationElement`（interface）。工厂/静态方法：of, ofClass, ofString, ofLong, ofInt, ofChar, ofShort, ofByte。查询/实例方法：name, value。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `AnnotationElementTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.AnnotationElement` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AnnotationElementTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.AnnotationElement")` + public/interface/继承 + 关键成员。黑盒（`AnnotationElementTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，16→81 断言）：14 工厂族矩阵+null 语义+顺序保持
新增 testFactoryMatrix（of(String,AV)/of(Utf8Entry,AV)/ofClass/ofString/ofLong/ofInt/ofChar/ofShort/ofByte/ofBoolean/ofDouble/ofFloat/ofAnnotation/ofArray 全族：name stringValue+value instanceof Of*+tag 精确（'B','C','S','Z','I','J','F','D','s','c','@','['）+类型化访问器逐值（含 OfString.constant() 为 Utf8Entry、OfClass className/classSymbol、OfArray values()、OfAnnotation annotation()））；testNulls（of(String,null)/of((String)null,v)/ofString(null,..)/ofAnnotation(null)/ofArray(null varargs)/ofClass(null) 全 NPE（实测））；testOrderPreservationRoundTrip（8 元素（byte..double）注解 build→re-parse：名字顺序+tag 顺序+值逐位全等）；testDeepNesting（array 内嵌 annotation+array 的二级聚合回读）。
