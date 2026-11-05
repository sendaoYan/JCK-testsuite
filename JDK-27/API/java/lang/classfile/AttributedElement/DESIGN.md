# java.lang.classfile.AttributedElement — 设计案

## 1. API 功能
`java.lang.classfile.AttributedElement`（interface）。工厂/静态方法：(none)。查询/实例方法：attributes, findAttribute, findAttributes。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `AttributedElementTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.AttributedElement` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AttributedElementTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.AttributedElement")` + public/interface/继承 + 关键成员。黑盒（`AttributedElementTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，12→35 断言）：查找/只读/实现者全契约
新增 testFindNegative（findAttribute(不存在)→empty（类/字段两级）、findAttributes(缺失)→空表非 null、findAttributes(deprecated) 双 Deprecated 全回（实测可共存 2 个）、findAttribute(null)→NPE）；testAttributesReadOnly（attributes() add/remove/clear → UnsupportedOperationException 只读快照（类/方法/字段三级实测）、拒绝后模型不变）；testImplementors（ClassModel/FieldModel/MethodModel/CodeModel 四类 instanceof AttributedElement、四类实例统一 List.of(...) 多态遍历属性名（SourceFile/ConstantValue/Code/StackMapTable 各归其位）、findAttribute 与 attributes() 自洽、is-a ClassFileElement）。
