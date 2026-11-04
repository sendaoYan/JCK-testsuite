# java.lang.reflect.AnnotatedArrayType — 测试设计（JDK-27）

## 1. API 功能
`AnnotatedArrayType` 表示数组类型的注解视图（`getAnnotatedType()` 于数组字段/返回值/参数时返回）：
- `getAnnotatedGenericComponentType()`：分量类型的 `AnnotatedType`（递归：多维每层剥一维）。
- 继承 `AnnotatedType`：`getType/getAnnotation/getAnnotations/getAnnotatedOwnerType`（数组无宿主 → null）。

## 2. Java 用户如何使用
声明 `@Sink String[] arr` 等类型使用注解 → `field.getAnnotatedType()` 强转 `AnnotatedArrayType` → `getAnnotatedGenericComponentType().getAnnotation(...)` 读元素级约束（Bean Validation 容器元素校验、序列化器元素编解码标记）。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/annotation/typeUse/`：数组分量上的 TYPE_USE 可见性。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/sun/reflect/annotation/AnnotatedArrayTypeImpl.java`：分量与注解持有权威实现。
- Hibernate Validator `ContainerElementTypeDescriptor`：`List<@NotNull String>`/`@NotNull String[]` 元素约束发现（同构场景 2/5）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AnnotatedArrayTypeTest.testBlackBox`，自定义 `@Sink(TYPE_USE)`，固定类 `Types`）：
1. 基本类型数组：分量 int、无注解。
2. `@Sink String[]`：分量携带 @Sink（get/isAnnotationPresent）、数组类型本身无注解。
3. 普通引用数组：分量 String 无注解、底层类型 `String[].class`。
4. `String[][]` 递归：内层仍为 AnnotatedArrayType、逐层剥到 String。
5. `List<String>[]`：分量为 `AnnotatedParameterizedType`、底层 PT、typeName 精确。
6. 方法返回值与参数两条获取路径（含参数分量注解）。
7. 数组 `getAnnotatedOwnerType()` null。
白盒保留原返回类型断言 + 接口结构。

Part 3 补充契约维度（`testDimensionAnnotations/testPrimitiveFamily/testRawAnnotatedGenericComponent`，自造 `@Mark(TYPE_USE)`，fixture `DimTypes`）：
1. 维度注解（核心）：`String @Sink [] @Mark []` **最左注解属最外层**（外层 Sink、内层 Mark），三层 `@Sink/@Mark/@Sink` 逐层剥；getType() 逐层精确 `String[][].class`/`String[].class`。
2. 8 原生家族（bool/byte/char/short/long/float/double，int 已在 Part 2）：精确数组 Class、精确组件、双层干净。
3. raw 级注解组件：`@Mark List<@Sink String>[]`：@Mark 落在组件 APT 自身、@Sink 在实参、数组层干净。

产出：`AnnotatedArrayTypeTest.java`（63 断言通过，jtreg 全绿）。
