# java.lang.reflect.AnnotatedWildcardType — 测试设计（JDK-27）

## 1. API 功能
`AnnotatedWildcardType` 表示通配符的注解视图（`? extends @Sink Number`）：
- `getAnnotatedUpperBounds()`：上界 `AnnotatedType[]`（含界上的 TYPE_USE 注解；`super`/无界的隐式 `[Object]` 无注解）。
- `getAnnotatedLowerBounds()`：下界（`extends`/无界为空）。
- 继承 `AnnotatedType`（本体无注解、owner null）。

## 2. Java 用户如何使用
从 `AnnotatedParameterizedType.getAnnotatedActualTypeArguments()` 取出 → 检查界注解（`? extends @Valid Number` 的容器元素校验），上/下界分别驱动读写方向判定。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/annotation/typeUse/`：通配符界的注解读取。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/sun/reflect/annotation/AnnotatedWildcardTypeImpl.java`：隐式 Object 上界权威实现。
- Hibernate Validator 容器元素约束发现（`List<? extends @NotNull Number>`）同构场景 1/4。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AnnotatedWildcardTypeTest.testBlackBox`，自定义 `@Sink(TYPE_USE)`，固定类 `Types`）：
1. `? extends @Sink Number`：上界 Number 带 @Sink、零下界。
2. `? super @Sink Integer`：下界 Integer 带 @Sink、隐式 `[Object]` 上界且无注解。
3. 无界 `?`：隐式 Object、零下界、界无注解。
4. `Map<? extends @Sink String, ? super @Sink Long>` 双通配分别标注、底层 typeName 非空。
5. 方法签名路径（`take(List<? super @Sink Long>)`）与字段路径一致。
6. 通配符本体无注解、owner null。
白盒保留原方法参数断言 + 接口结构。产出：`AnnotatedWildcardTypeTest.java`（24 断言通过）。
