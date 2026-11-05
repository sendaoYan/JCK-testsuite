# java.lang.classfile.AnnotationValue.OfFloat — 设计案

## 1. API 功能
`java.lang.classfile.AnnotationValue.OfFloat`（interface）。工厂/静态方法：(none)。查询/实例方法：constant, floatValue, resolvedValue。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `OfFloatTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.AnnotationValue.OfFloat` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`OfFloatTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.AnnotationValue$OfFloat")` + public/interface/继承 + 关键成员。黑盒（`OfFloatTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 契约深化补遗（2026-11-05，12→51 断言）

- tag 'F'==70；ofFloat(FloatEntry) 条目同一性复用，池条目 tag=TAG_FLOAT(4)；NaN 经 resolvedValue 保持 NaN。
- 特殊值序列化往返 **raw bits 保真**：-0.0f 位模式 0x80000000、NaN、±Inf、Float.MIN/MAX_VALUE 全等。
- **equals 为 IEEE == 语义（实测）**：NaN equals NaN false（hashCode 仍一致）；-0.0f equals 0.0f true。
- javac 真类 @FloatAnno(f=-0.0f, g=3.25f)（bits 断言）+ default 0.5f；注解语法不允许 NaN/Inf 字面量（非编译期常量），NaN 只能经 API 工厂产生。
- 新增维度（testDeep* 系列，仅追加不删既有断言）：
  1. 工厂全矩阵：值形态 + 池条目形态（条目同一性复用 `constant() == 原条目`）+ tag 精确字符值；
  2. build→re-parse 全等闭环：`ClassFile.of().build(... with(RuntimeVisibleAnnotationsAttribute.of(...)))` → parse → 逐元素 instanceof + 值/tag 全等（注解真序列化往返）；
  3. 边界值矩阵；
  4. null 负例（精确异常类型 + 关键消息片段）；
  5. 层次链 `AV ← OfConstant ← OfX` 与跨形态不相交（`Of*.class.isInstance` 运行时判定；OfEnum/OfClass/OfArray/OfAnnotation 对 OfConstant 的 instanceof 在**编译期即被 javac 证伪报错**，只能用 isInstance）；
  6. equals/hashCode/toString 实测；
  7. javac 真类产物闭环：嵌套 @interface（@Retention(RUNTIME)）+ 被注解字段 → RuntimeVisibleAnnotations 模型逐元素回读 + @interface 方法上的 AnnotationDefault 值回读 + 无 default 成员无该属性 + "仅显式元素被序列化（default 不出现）"。
