# java.lang.reflect.Constructor — 测试设计（JDK-27）

## 1. API 功能
`Constructor<T>` 表示类的构造器（经 `Class.getConstructor/getDeclaredConstructor` 获得）：
- 元数据：`getName/getDeclaringClass/getModifiers/getParameterTypes/getParameterCount/getGenericParameterTypes/getExceptionTypes/getTypeParameters/isVarArgs/isSynthetic`。
- 实例化：`newInstance(Object... initargs)`，异常 `InstantiationException/IllegalAccessException/IllegalArgumentException/InvocationTargetException`。
- 注解与泛型：`getAnnotation/getDeclaredAnnotations/getParameterAnnotations/getAnnotatedReturnType/getAnnotatedReceiverType`。
- 相等与展示：`equals/hashCode/toString/toGenericString`。

## 2. Java 用户如何使用
`getConstructor(精确参数类型...)` → `newInstance(实参...)` → 校验新对象状态；私有构造器先 `setAccessible(true)`（单例破坏、框架实例化）。典型链路：
- JSON 反序列化（Jackson 无参构造）、DI 容器（Spring `BeanUtils.instantiateClass`）。
- `getDeclaredConstructor` + `setAccessible` 实例化仅有私有构造的工具类。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Constructor/Equals.java`：构造器值语义。
- JDK 自测 `Constructor/GenericStringTest.java`：`toGenericString` 形态。
- JDK 自测 `Constructor/TestParameterAnnotations.java`：`getParameterAnnotations`。
- Spring `BeanUtils.instantiateClass(Constructor, Object...)`：可达性设置 + 异常解包（`InvocationTargetException`）再抛根因——与场景 3 同构。
- Commons Lang `ConstructorUtils.invokeConstructor`：按实参匹配构造器并实例化（场景 1/6）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ConstructorTest.testBlackBox`，固定类 `Widget/Gadget/AbstractThing/Color`/顶层类 `CtorSeparate`）：
1. 精确签名构造 + 字段状态断言 + 无参链式构造（参数计数/类型/修饰符/异常表）。
2. varargs 构造器：`isVarArgs`、参数类型 `String[]`、打包数组与单数组两种 `newInstance` 形态。
3. 构造器抛异常 → `InvocationTargetException`（cause 精确类型+消息）；同一私有构造器 `setAccessible(true)` 后成功。
4. 跨 nest 私有构造器：先 `IllegalAccessException`，`setAccessible` 后成功。
5. 抽象类 → `InstantiationException`；接口无构造器（长度 0）；枚举构造器 `newInstance` → `IllegalArgumentException`。
6. 参数数量/类型错配 → `IllegalArgumentException`。
7. 泛型构造器：擦除参数 `Object.class`、`newInstance` 返回携带 payload、自身 `getTypeParameters` 为空、`getAnnotatedReturnType` 为声明类。
8. `equals/hashCode` 值语义、`toString/toGenericString` 含类名、声明构造器计数。
白盒保留原结构断言 + final/Executable 超类。产出：`ConstructorTest.java`（44 断言通过）。
