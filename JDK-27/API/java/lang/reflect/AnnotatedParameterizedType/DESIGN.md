# java.lang.reflect.AnnotatedParameterizedType — 测试设计（JDK-27）

## 1. API 功能
`AnnotatedParameterizedType` 表示参数化类型的注解视图（`List<@Sink String>`）：
- `getAnnotatedActualTypeArguments()`：各实参的 `AnnotatedType[]`（可递归嵌套）。
- `getAnnotatedOwnerType()`：内部类型宿主。
- 继承 `AnnotatedType`：`getType()` 返回底层 `ParameterizedType`（含 raw/args）。

## 2. Java 用户如何使用
`field.getAnnotatedType()` 强转 → 逐实检查注解（容器元素约束 `List<@NotNull String>`、`Map<@Sink K, @Sink V>` 双键值标注、深层嵌套 `Map<String, List<@Sink Integer>>`）；实参数组长度与底层 PT 一致性可用于健壮性校验。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/annotation/typeUse/` 与 `test/jdk/java/lang/reflect/`：TYPE_USE 实参注解的读取路径。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/sun/reflect/annotation/AnnotatedParameterizedTypeImpl.java`。
- Hibernate Validator `ContainerElementTree`：`getAnnotatedActualTypeArguments` 逐层发现容器元素约束（同构场景 2/3/4）。
- Jackson `JavaType` 注解桥接（contentConverter 等类型注解）同构。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AnnotatedParameterizedTypeTest.testBlackBox`，自定义 `@Sink(TYPE_USE)`，固定类 `Types`）：
1. 普通 PT：实参 String 无注解、底层 PT raw=List。
2. `List<@Sink String>`：实参携带注解、外层类型本身无注解。
3. `Map<@Sink String, @Sink Integer>`：双实参独立标注与类型。
4. 嵌套 `Map<String, List<@Sink Integer>>`：递归两层取最内实参注解。
5. `Map.Entry` 宿主：`getAnnotatedOwnerType().getType()==Map`。
6. 方法参数路径的实注解实参。
7. 全 fixture 实参数组长度与底层 `ParameterizedType` 一致性循环断言。
白盒保留原返回类型断言 + 接口结构。产出：`AnnotatedParameterizedTypeTest.java`（28 断言通过）。
