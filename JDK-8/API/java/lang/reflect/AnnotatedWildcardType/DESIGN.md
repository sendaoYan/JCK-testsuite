# java.lang.reflect.AnnotatedWildcardType — 测试设计（JDK-8）

## 1. API 功能
`AnnotatedWildcardType` 表示通配符的注解视图（JDK 8 接口 `getAnnotatedUpperBounds()`/`getAnnotatedLowerBounds()`）：
- 上界数组（`? extends X` 显式/无界隐式 Object）；下界数组（`? super X`，extends/无界为空）。
- 继承 `AnnotatedType`：`getType()` 是底层 `WildcardType`（getUpperBounds/getLowerBounds 交叉验证）；**通配符自身注解**（`List<@A ?>`）挂在本 AWT 上，与界上注解（`? extends @A Number`）是两个挂点。
- 取得入口：`AnnotatedParameterizedType.getAnnotatedActualTypeArguments()[i]` 强转。

## 2. Java 用户如何使用
容器元素读写方向校验（`List<? extends @Valid Number>` 上界约束、`? super @Inject T` 下界注入）、类型注解处理器区分"注解在通配符上"还是"在界上"。

## 3. 参考来源
- Oracle javadoc（Java 8 `AnnotatedWildcardType`、`WildcardType`、JLS 4.5.1）。
- OpenJDK 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/AnnotatedWildcardType.java`、`sun/reflect/annotation/AnnotatedWildcardTypeImpl.java`（隐式界补全逻辑）。
- OpenJDK 回归：`~/git/jdk/test/jdk/java/lang/annotation/typeUse/`（通配符界用例）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/annotation/`。

## 4. 测试用例设计 + 易错规范细节
fixture：`@A/@B(TYPE_USE)`；字段 `List<? extends @A Number>`/`List<? super @B Integer>`/`List<?>`/`List<@A ?>`/`Map<? extends @A String, ? super @B Long>`/`List<List<? super @B Integer>>`；方法返回 `List<? extends @A Number>`、参数 `List<? super @B Long>`。

| test 方法 | 场景 | 关键断言 |
|---|---|---|
| testAnnotatedWildcardType | 白盒保留 | 参数 wildcard 上界 Number/无下界 |
| testUpperBound | extends 分支 | 上界 Number 带 @A（四查询）、无下界、本体干净、底层 WildcardType 上下界一致 |
| testLowerBound | super 分支 | 下界 Integer 带 @B；**本 JDK 上界数组为空**（实测记录）；底层 raw WildcardType 仍有隐式 Object 上界 |
| testUnbounded | 无界 | 恰 1 隐式 Object 上界、无下界、全部干净 |
| testSelfAnnotated | 通配符自身注解 | `List<@A ?>`：@A 在 AWT 自身、界仍隐式 Object 且无注解 |
| testTwoWildcards | 双通配符 | Map 的 key 上界 @A String、value 下界 @B Long，各自独立 |
| testMethodPaths | 方法路径 | 返回/参数通配符注解同构字段 |
| testDeepNesting | 深嵌套 | `List<List<? super @B Integer>>` 内层下界注解仍在 |

易错规范细节（实测锁定）：
1. **版本差异核心**：`? super X` 的 `getAnnotatedUpperBounds()` 在 jdk8u372-b07 返回**空数组**，在 9+ 返回 `[Object]`；而底层 `WildcardType.getUpperBounds()` 双侧都返回 `[Object]`——8 侧据此分别断言。
2. 无界 `?` 的上界是隐式 `[Object]`（带一条，与 super 分支不同），下界恒空。
3. 通配符自身注解（`@A ?`）与界注解（`? extends @A Number`）挂点不同：前者 AWT.getAnnotations()，后者 bounds[i].getAnnotations()。
4. 通配符只能经参数化实参取得（没有裸 wildcard 字段），先 APT 再下钻。
5. 元注解只出现在代码里（jtreg 头注释禁用）。

产出：`AnnotatedWildcardTypeTest.java`（47 断言通过，jtreg 全绿）。
