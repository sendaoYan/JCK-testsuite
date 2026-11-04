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
白盒保留原方法参数断言 + 接口结构。产出：`AnnotatedTypeVariableTest.java`（29 断言通过）。
