# java.lang.reflect.AnnotatedArrayType — 测试设计（JDK-8）

## 1. API 功能
`AnnotatedArrayType` 表示数组类型的注解视图（JDK 8 接口仅 `getAnnotatedGenericComponentType()`；owner 访问器是 9+）：
- `getAnnotatedGenericComponentType()`：剥一维后的组件 `AnnotatedType`（可能是 Class 视图、AnnotatedArrayType、AnnotatedParameterizedType）。
- 继承 `AnnotatedType`：`getType()`==`Xxx[].class` 精确数组 Class、注解查询在**本维度**上。
- JSR 308 维度注解：`String @A [] @B []` 中每个注解绑定一个数组维度。

## 2. Java 用户如何使用
数组元素校验（`List<@Valid String>[]`、`@NotEmpty String[]` 容器元素约束）、序列化器数组编解码标记、类型注解处理器扫描维度级注解；与 `Array` 反射配合先看组件类型再读写元素。

## 3. 参考来源
- Oracle javadoc（Java 8 `AnnotatedArrayType`、JLS 9.7.4 数组维度注解绑定规则）。
- OpenJDK 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/AnnotatedArrayType.java`、`sun/reflect/annotation/AnnotatedArrayTypeImpl.java`。
- OpenJDK 回归：`~/git/jdk/test/jdk/java/lang/annotation/typeUse/`（数组维度用例）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/annotation/`。

## 4. 测试用例设计 + 易错规范细节
fixture：`@A/@B(TYPE_USE)`、8 种原生数组字段、`@A String[]`、`String @A [] @B []`、三层 `String @A [] @B [] @A []`、`List<@A String>[]`、`@B List<String>[]`、`String[][]`、方法 `int @A [] returnsAnnotatedArray()`/参数 `String @B []`。

| test 方法 | 场景 | 关键断言 |
|---|---|---|
| testAnnotatedArrayType | 白盒保留 | 返回类型 instanceof/getType/组件 |
| testPrimitiveComponents | 8 原生全表 | boolean/byte/char/short/int/long/float/double：getType()==精确数组 Class、组件==原生 Class、双层无注解 |
| testAnnotatedComponent | 组件注解 | `@A String[]`：数组层无注解、组件 String 带 @A（四查询） |
| testDimensionAnnotations | 维度注解（核心） | `String @A [] @B []`：外层（最左注解）带 A、组件层带 B；三层 A/B/A 逐层；getType() 逐层 `String[][].class`/`String[].class` |
| testGenericComponent | 泛型组件 | `List<@A String>[]`：组件 instanceof APT、raw List、实参带 @A；`@B List<String>[]`：raw 级 @B 落在组件 APT 自身、数组层干净 |
| testMethodPaths | 方法路径 | 返回 `int @A []`（维度注解+组件 int）；参数 `String @B []` 同构 |
| testNested | 无注解嵌套 | 逐层剥维、getType 精确 |

易错规范细节（实测锁定）：
1. **维度绑定方向**：`String @A [] @B []` 的**最左注解属于最外层**数组类型（getAnnotatedType() 返回的 AAT 带 @A），其组件（String[] 层）带 @B——直觉相反，最易写错。
2. `@A String[]` 中注解落在**组件**（String 上），数组层 `getAnnotations()` 为空；注解要落在数组层需写 `String @A []`。
3. raw 级注解（`@B List<String>`）在数组场景下落在组件 APT 自身，数组层仍干净。
4. `getType()` 恒为精确数组 Class（`String[][].class`），可 == 断言。
5. 元注解只出现在代码里（jtreg 头注释禁用）。

产出：`AnnotatedArrayTypeTest.java`（88 断言通过，jtreg 全绿）。
