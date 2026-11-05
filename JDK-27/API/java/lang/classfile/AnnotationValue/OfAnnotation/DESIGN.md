# java.lang.classfile.AnnotationValue.OfAnnotation — 设计案

## 1. API 功能
`java.lang.classfile.AnnotationValue.OfAnnotation`（interface）。工厂/静态方法：(none)。查询/实例方法：annotation。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `OfAnnotationTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.AnnotationValue.OfAnnotation` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`OfAnnotationTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.AnnotationValue$OfAnnotation")` + public/interface/继承 + 关键成员。黑盒（`OfAnnotationTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 契约深化补遗（2026-11-05，10→38 断言）

- `annotation() == 原实例`（同一性）；tag '@'==64；ofAnnotation(null) NPE。
- **OfAnnotation 不属 OfConstant**。
- 序列化往返：一级嵌套（多元素全等）+ 二级嵌套（L1→L2→L3 深度回读）。
- javac 真类：@AnnoAnno(n=@InnerAnno(q=5), m=@InnerAnno)——`m = @InnerAnno` 无实参时内层注解 **elements() 为空**（q 的 default 不序列化）；AnnotationDefault n()==@InnerAnno(q=9)。
- 新增维度（testDeep* 系列，仅追加不删既有断言）：
  1. 工厂全矩阵：值形态 + 池条目形态（条目同一性复用 `constant() == 原条目`）+ tag 精确字符值；
  2. build→re-parse 全等闭环：`ClassFile.of().build(... with(RuntimeVisibleAnnotationsAttribute.of(...)))` → parse → 逐元素 instanceof + 值/tag 全等（注解真序列化往返）；
  3. 边界值矩阵；
  4. null 负例（精确异常类型 + 关键消息片段）；
  5. 层次链 `AV ← OfConstant ← OfX` 与跨形态不相交（`Of*.class.isInstance` 运行时判定；OfEnum/OfClass/OfArray/OfAnnotation 对 OfConstant 的 instanceof 在**编译期即被 javac 证伪报错**，只能用 isInstance）；
  6. equals/hashCode/toString 实测；
  7. javac 真类产物闭环：嵌套 @interface（@Retention(RUNTIME)）+ 被注解字段 → RuntimeVisibleAnnotations 模型逐元素回读 + @interface 方法上的 AnnotationDefault 值回读 + 无 default 成员无该属性 + "仅显式元素被序列化（default 不出现）"。
