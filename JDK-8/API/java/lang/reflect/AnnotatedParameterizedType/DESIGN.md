# java.lang.reflect.AnnotatedParameterizedType — 测试设计（JDK-8）

## 1. API 功能
`AnnotatedParameterizedType` 表示参数化类型的注解视图（JDK 8 接口仅 `getAnnotatedActualTypeArguments()`）：
- `getAnnotatedActualTypeArguments()`：各泛型实参的 `AnnotatedType[]`（可再嵌套 APT/ATV/AWT）。
- 继承 `AnnotatedType`：`getType()` 是底层 `ParameterizedType`（rawType/actualTypeArguments/ownerType 精确）；**raw 级注解**（`@B List<String>` 的 @B）挂在本 APT 自身。
- 负例：raw 用法（`List` 无 <>）**不是** AnnotatedParameterizedType。

## 2. Java 用户如何使用
容器元素校验（Hibernate Validator 的 `List<@Valid String>`、`Map<@Valid Key, @Valid Value>`）、注入泛型限定、序列化器 contentUsing 标记：先取 APT 再逐实检查注解；raw 级注解区分"对整个容器"还是"对元素"。

## 3. 参考来源
- Oracle javadoc（Java 8 `AnnotatedParameterizedType`、`ParameterizedType`）。
- OpenJDK 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/AnnotatedParameterizedType.java`、`sun/reflect/annotation/AnnotatedParameterizedTypeImpl.java`。
- OpenJDK 回归：`~/git/jdk/test/jdk/java/lang/annotation/typeUse/`、`~/git/jdk/test/jdk/java/lang/reflect/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/annotation/`。

## 4. 测试用例设计 + 易错规范细节
fixture：`@A/@B(TYPE_USE)`、字段 `List<String>`/`List<@A String>`/`@B List<String>`/`List rawList`/`Map<@A String, @B List<@A Integer>>`/三层嵌套 `Map<String, Map<String, List<@A String>>>`/`Map.Entry<String,Integer>`；方法返回 `Map<@A String, @B List<Integer>>`、参数 `List<@A String>`。

| test 方法 | 场景 | 关键断言 |
|---|---|---|
| testAnnotatedParameterizedType | 白盒保留 | 返回类型 instanceof/实参 |
| testPlainUnderlying | 底层契约 | getType() ParameterizedType：rawType==List、actualTypeArguments[0]==String.class、owner null；实参数量与底层一致 |
| testAnnotatedArgument | 实参注解 | `List<@A String>`：实参四查询命中、外层干净 |
| testRawTypeAnnotation | raw 级注解 | `@B List<String>`：@B 在 APT 自身（getAnnotation/isAnnotationPresent/getAnnotations），实参干净 |
| testRawListNotParameterized | 负例 | raw `List` 字段：**非** APT、getType()==List.class、getGenericType()==List.class |
| testNestedArguments | 嵌套双注解 | `Map<@A String, @B List<@A Integer>>`：key A、value 是 APT 且 raw 级 B、最内实参 A；三层深嵌套最底层注解仍在 |
| testOwnerType | 宿主 | `Map.Entry`：底层 ParameterizedType.getOwnerType()==Map（注解视图 owner 访问器 9+） |
| testMethodPaths | 方法路径 | 返回/参数注解位置同构 |
| testArgCountMatchesUnderlying | 交叉 | 6 个 fixture 的 annotated 实参数量 == 底层 PT actualTypeArguments 数量 |

易错规范细节（实测锁定）：
1. raw 级注解（`@B List<String>`）与实参注解（`List<@A String>`）是**两个不同挂点**，互不渗透：前者在 APT.getAnnotations()，后者在 args[i].getAnnotations()。
2. raw 用法 `List f;` 不产生 APT——getAnnotatedType() 返回普通 AnnotatedType（getType()==List.class），这是最常写错的负例。
3. 嵌套参数化实参（`Map<String, List<@A Integer>>` 的 value）自身是 APT，可继续 getAnnotatedActualTypeArguments 下钻。
4. `getAnnotatedActualTypeArguments().length` 恒等于底层 `getActualTypeArguments().length`，可作交叉断言。
5. 元注解只出现在代码里（jtreg 头注释禁用）。

产出：`AnnotatedParameterizedTypeTest.java`（55 断言通过，jtreg 全绿）。
