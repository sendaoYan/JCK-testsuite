# java.lang.reflect.AnnotatedTypeVariable — 测试设计（JDK-8）

## 1. API 功能
`AnnotatedTypeVariable` 表示类型变量的注解视图（JDK 8 接口仅 `getAnnotatedBounds()`；owner 访问器 9+）：
- `getAnnotatedBounds()`：声明处各界的 `AnnotatedType[]`（界上可带 TYPE_USE/TYPE_PARAMETER 注解）。
- 继承 `AnnotatedType`：`getType()` 是底层 `TypeVariable`（getName/getGenericDeclaration/getBounds）；**使用点注解**（`@A T x`）挂在本 ATV 自身。
- 使用点 vs 声明点：字段/方法/构造器使用 `T` 处得到 ATV（界来自声明）；`Class/Method.getTypeParameters()` 得到 `TypeVariable`（`getAnnotatedBounds` 查声明界）。

## 2. Java 用户如何使用
泛型容器元素校验（`T extends @Valid Number` 界约束）、注入框架的泛型限定扫描、类型注解处理器区分"注解在使用处的 T 上"与"注解在 T 的界上"两类语义。

## 3. 参考来源
- Oracle javadoc（Java 8 `AnnotatedTypeVariable`、`TypeVariable.getAnnotatedBounds`、JLS 4.4/9.7.4）。
- OpenJDK 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/AnnotatedTypeVariable.java`、`TypeVariable.java`、`sun/reflect/annotation/TypeVariableImpl.java`。
- OpenJDK 回归：`~/git/jdk/test/jdk/java/lang/annotation/typeUse/`（界注解用例）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/annotation/`。

## 4. 测试用例设计 + 易错规范细节
fixture：`@A(TYPE_USE)`、`@Bound(TYPE_USE, TYPE_PARAMETER)`；`Holder<T>{T value; Holder(T); T get()}`、`AnnotatedUse<T>{@A T tagged}`、`Bounded<T extends @Bound Number & Comparable<T>>`、`Free<T>`；方法 `<T extends Number> typeVar`/`<T extends @Bound Number> annotatedBound`。

| test 方法 | 场景 | 关键断言 |
|---|---|---|
| testAnnotatedTypeVariable | 白盒保留 | 参数 ATV/单界 Number |
| testFieldUse | 字段使用点 | `T value`：ATV、getType() TypeVariable 名 "T"、无注解、隐式单界 Object |
| testAnnotatedUseSite | 使用点注解 | `@A T tagged`：ATV 自身四查询命中、界不受影响（仍 Object） |
| testMethodAndCtorPaths | 返回/构造器 | `T get()` 返回 ATV；`Holder(T)` 构造器参数 ATV、隐式界 Object |
| testClassDeclarationBounds | 类声明界 | `Bounded`：2 界、[0] Number 带 @Bound、[1] ParameterizedType raw Comparable 无注解、getGenericDeclaration==类 |
| testMultiBoundUseSite | 使用点界+方法 TV | `annotatedBound` 参数：界 Number 带 @Bound；`m.getTypeParameters()` 名 T、getGenericDeclaration、界注解同 |
| testUnbounded | 无界 | `Free<T>`：恰 1 隐式 Object 界、无注解 |
| testTypeVariableContract | 底层契约 | TypeVariable：name/getGenericDeclaration/getBounds[0]==Object/is-a-Type |

易错规范细节（实测锁定）：
1. **使用点 vs 声明点**：字段 `T x` 的 `getAnnotatedBounds()` 反映**声明处**界；`@A T` 的 @A 在 ATV 自身（getAnnotations），不在界上。
2. 无界 `<T>` 的 getAnnotatedBounds() 恰含 1 个 Object（隐式界），不是空数组。
3. 多界 `T extends A & B`：界数组保序，[1] 是 ParameterizedType（`Comparable<T>`），注解只挂在写的位置上。
4. `getGenericDeclaration()` 返回与 `Class.getTypeParameters()` 一致的声明者；Method 场景下 `getTypeParameters()[0].getGenericDeclaration() == m`（8 实现共享同一 Method 实例；9+ 需用 equals 比较）。
5. 界注解需要注解同时具备 TYPE_USE 与 TYPE_PARAMETER 目标才能两处可见。
6. 元注解只出现在代码里（jtreg 头注释禁用）。

产出：`AnnotatedTypeVariableTest.java`（52 断言通过，jtreg 全绿）。
