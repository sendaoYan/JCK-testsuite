# java.lang.reflect.AnnotatedType — 测试设计（JDK-27）

## 1. API 功能
`AnnotatedType` 表示可能带类型使用（TYPE_USE）注解的类型：
- `getType()`：底层 `Type`（Class/PT/TV/GAT）。
- 注解查询（实现 `AnnotatedElement` 的精简集）：`getAnnotation/getAnnotations/getDeclaredAnnotations`。
- 默认方法 `getAnnotatedOwnerType()`：内部类型的宿主 `AnnotatedType`，顶层返回 null。
- 入口：`Field.getAnnotatedType/Method.getAnnotatedReturnType/Parameter.getAnnotatedType/Executable.getAnnotatedParameterTypes` 等。

## 2. Java 用户如何使用
在类型位置（字段/返回值/参数/泛型实参）上读取 `@Target(TYPE_USE)` 注解：校验框架（Bean Validation 的容器元素校验）、依赖注入的类型限定、序列化器的自定义编解码标记。`getAnnotatedOwnerType` 处理内部类宿主。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/annotation/typeUse/`（TypeUseTests 等）：TYPE_USE 在字段/方法/参数的可见性（同构场景 2/3/6）。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/AnnotatedType.java`：owner 语义权威。
- Hibernate Validator 6+ 容器元素约束：`getAnnotatedType` + `AnnotatedParameterizedType` 驱动 `List<@Valid String>` 校验（对应场景 5/6）。
- Jackson 自定义 `@JsonSerialize(contentUsing=...)` 类型注解：同构读取路径。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AnnotatedTypeTest.testBlackBox`，自定义 `@Sink(TYPE_USE)`，固定类 `Types` 与方法）：
1. 普通类型：底层类型正确、无注解（4 项查询全空/false）。
2. `@Sink String` 字段：注解四查询全命中。
3. 方法返回类型同构（带注解/不带注解对照）。
4. 宿主：`Map.Entry` 字段 `getAnnotatedOwnerType().getType()==Map`；顶层类型 null。
5. 参数化底层：`List` 字段底层为 PT 且与 `getGenericType` 一致。
6. 参数注解类型：`@Sink String` 参数可见。
7. 默认方法族（byType 空）与 `getAnnotation(null)` NPE。
白盒保留原返回类型断言 + 接口结构（含 AnnotatedType 不继承 Type 的包装关系）。产出：`AnnotatedTypeTest.java`（29 断言通过）。
