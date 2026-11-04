# java.lang.reflect.AnnotatedTypeVariable — 测试设计（JDK-27）

## 1. API 功能
`AnnotatedTypeVariable` 表示类型变量使用处的注解视图（`Holder<T>` 的 `T` 字段/返回值/参数）：
- `getType()`：底层 `TypeVariable`。
- `getAnnotatedBounds()`：界上的 `AnnotatedType[]`（镜像声明处的界与注解；无界变量为隐式 `[Object]`）。
- `getAnnotatedOwnerType()`：类型变量无宿主 → null。
- 继承 `AnnotatedType` 注解查询。

## 2. Java 用户如何使用
泛型字段/方法/参数经 `getAnnotatedType()` 判为 ATV → 检查使用处注解与界注解（`T extends @Bound Number`），供校验器/序列化器按界分派处理；隐式 Object 界是重要边界。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/annotation/typeUse/`：类型变量及其界的 TYPE_USE 注解可见性。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/sun/reflect/annotation/AnnotatedTypeVariableImpl.java`。
- Spring `ResolvableType` 变量界解析；Hibernate Validator 泛型界校验（同构场景 3/4）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AnnotatedTypeVariableTest.testBlackBox`，自定义 `@Use(TYPE_USE)`/`@Bound(TYPE_USE+TYPE_PARAMETER)`，固定类 `Holder<T>`/`Bounded<T extends @Bound Number & Comparable<T>>`）：
1. 字段 `T value`：ATV、底层 TV 名 T、无使用注解、owner null。
2. 泛型方法返回值同为 ATV。
3. 声明处界：双界 `[Number, Comparable<T>(PT)]`、第一界带 `@Bound`、第二界无。
4. 使用处界镜像：`<T extends @Bound Number>` 方法参数界注解可见。
5. 无界变量：隐式 `[Object]` 且无注解。
6. TYPE_USE 使用处注解查询面（空对照）。
7. 声明处 TV 本身的注解查询空。
白盒保留原方法参数断言 + 接口结构。

Part 3 补充契约维度（`testAnnotatedUseSite/testConstructorPath/testMethodTypeParameters/testClassDoubleBound`，fixture：`AnnotatedUse<T>{@Use T tagged}`、`WithCtor<T extends Number>(T)`）：
1. **使用点注解**：`@Use T tagged` 字段：@Use 在 ATV 自身（四查询）、界仍隐式 Object。
2. 构造器路径：`WithCtor(T)` 参数 ATV、名 "T"、单界 Number、本体干净。
3. 方法类型参数表：`m.getTypeParameters()` 名/界注解/界类型；`getGenericDeclaration()` 与 `getMethod` 拷贝用 **equals**（反射对象每次拷贝，9+ 不可用 ==，8 可）。
4. 类级双界：`Bounded` 的 TV 两界保序、首界 @Bound、次界（PT Comparable）干净、raw getBounds[0]==Number。

产出：`AnnotatedTypeVariableTest.java`（51 断言通过，jtreg 全绿）。
