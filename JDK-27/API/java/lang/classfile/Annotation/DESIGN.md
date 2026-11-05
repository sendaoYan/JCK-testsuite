# java.lang.classfile.Annotation — 设计案

## 1. API 功能
`java.lang.classfile.Annotation`（interface）。工厂/静态方法：of。查询/实例方法：className, classSymbol, elements。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `AnnotationTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Annotation` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AnnotationTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Annotation")` + public/interface/继承 + 关键成员。黑盒（`AnnotationTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，13→53 断言）：工厂族+往返+真类回读
新增 testOfOverloadsAndNulls（of(cd) 空元素/of(cd,List.of())/of(Utf8Entry,varargs)/of(Utf8Entry,List) 四重载、className 是 Utf8Entry、classSymbol 全等、of(null)/of(cd,(List)null) NPE、elements() UOE 只读、池条目复用（==））；testBuildReparseRoundTrip（4 元素注解 build with(RuntimeVisibleAnnotationsAttribute.of)→re-parse：className/classSymbol/元素数/顺序（s,n,l,b）/逐值类型化全等（String/Int/Long/Boolean）、双注解顺序保持）；testNestedAnnotation（ofAnnotation 嵌套回读：tag '@'==64、嵌套 className/元素名/布尔值）；testRealDeprecatedMethod（真类 @Deprecated 方法自解析：恰 1 注解、className=="Ljava/lang/Deprecated;"、零元素、DeprecatedAttribute 并存（facts §6））。

## 5. 契约深化补遗（2026-11-05，57→129 断言）

- `of` 四重载 null 全矩阵：`(Utf8Entry)null`、varargs 含 null、List 含 null 元素均 NPE；`of(null, varargs)` **不经转型无法编译**（Utf8Entry/ClassDesc 两重载歧义，编译期事实）。
- equals/hashCode 为结构化（类名+元素名+元素值三元组逐项比较），同形对称且 hashCode 一致；toString 含类描述符（空元素形如 `Annotation[Lpkg/E;]`）。
- **全 13 形态 mega 注解真序列化往返**：一次 build 序列化 B/S/C/Z/I/J/F/D/s/c/e/[/@ 全形态 + 边界值（Byte.MIN、Short.MIN、'\0'、Int.MAX、Long.MIN、Float.NaN、Double.+Inf、空串、enum 两段、混合数组、嵌套注解），re-parse 后 13 元素保序、tag 序列精确 `BSCZIJFDsce@[`、逐值全等；同属性第二条空元素注解 `elements().isEmpty()` 保持。
- builder 挂载不止类级：`withField(..., fb -> fb.with(RVA))` 与 `withMethod(..., mb -> ... .with(RVA))` 均可往返回读。
- javac 真类全形态闭环：自造 `@interface FullAnno`（13 个带 default 成员覆盖全部 13 值形态 + 无 default 的 req）+ `@FullAnno(...)` 显式 12 元素标注字段：
  - 字段 RuntimeVisibleAnnotations：恰 12 个显式元素，**by/sh 走 default 不出现在序列化产物**（elemOf 为 null）；
  - @interface 类文件：14 个成员方法、13 个 AnnotationDefault，逐类型 instanceof + 值全等（v=7 OfInt、s="dflt"、b=false、c='x'、l=7L、d=1.25、f=0.5f、cl=L Runnable、e=SOURCE、arr={1,2}、by=3、sh=300、n=@InnerAnno(q=9)）；
  - @Retention(RUNTIME) 元注解落在 FullAnno 自身 RuntimeVisibleAnnotations（`Ljava/lang/annotation/Retention;`）；
  - 无 @Retention 的 `@interface ClassRet`（CLASS retention）→ 被注解字段走 **RuntimeInvisibleAnnotations**（RuntimeVisible 缺席），元素 w=6。
