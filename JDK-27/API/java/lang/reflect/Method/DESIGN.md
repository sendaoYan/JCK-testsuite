# java.lang.reflect.Method — 测试设计（JDK-27）

## 1. API 功能
`Method` 表示类/接口上的单个方法（无 public 构造器，经 `Class.getMethod/getDeclaredMethod` 获得）：
- 元数据查询：`getName/getDeclaringClass/getModifiers/getReturnType/getGenericReturnType/getParameterTypes/getParameterCount/getGenericParameterTypes/getExceptionTypes/getGenericExceptionTypes/getTypeParameters/getAnnotatedReturnType/getParameterAnnotations`。
- 调用：`invoke(Object obj, Object... args)`，可抛 `IllegalAccessException/IllegalArgumentException/InvocationTargetException/NullPointerException`。
- 形态标记：`isVarArgs/isBridge/isSynthetic/isDefault/isVarArgs`。
- 注解/默认值：`getDefaultValue/getAnnotation/getDeclaredAnnotations`。
- 访问控制：继承自 `Executable/AccessibleObject` 的 `setAccessible/trySetAccessible/canAccess`。
- 相等与展示：`equals/hashCode/toString/toGenericString`。

## 2. Java 用户如何使用
`Class.getDeclaredMethod("名", 精确参数类型...)` → 检查签名 →（私有方法 `setAccessible(true)`）→ `invoke(实例或 null(静态), 参数...)` → 处理返回值或 `InvocationTargetException.getCause()`。典型链路：
- 框架（Jackson/Spring `@Autowired` 注入、JUnit `@Test` 发现）按名+参数类型定位 → `invoke` 驱动业务。
- 序列化库用 `getReturnType/isDefault/getDefaultValue`（注解处理器）驱动配置。
- `isBridge` 用于泛型擦除产生的桥方法过滤，避免重复处理。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Method/IsDefaultTest.java`：接口 default 方法经 `isDefault()` 判定，随后 `invoke` 验证语义。
- JDK 自测 `Method/Equals.java`、`Method/GenericStringTest.java`：`equals/hashCode/toGenericString` 的值语义与字符串形态断言。
- JDK 自测 `Method/InheritedMethods.java`：方法表查找与签名匹配。
- Spring Framework `ReflectionUtils.findMethod/doWithMethods`（spring-core）：精确签名查找 + `makeAccessible` + `invoke` 是其依赖注入/事件分发的核心路径（同构于本测试第 1/2/7 步）。
- Apache Commons Lang `MethodUtils.invokeMethod`：变长参数匹配 + 可达性设置 + invoke 包装（对应第 6/7 步）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`MethodTest.testBlackBox`，固定业务类 `Biz`/`StringCmp`/`DefaultIface`/`@Marked`/顶层类 `MethodSeparate`）：
1. 精确签名查找 `add(int,int)` → invoke 2+3=5、负数、装箱返回 `Integer`（3 断言+修饰符/标记/异常表 4 断言）。
2. 静态方法 `greet`：null receiver、receiver 被忽略（2）。
3. 泛型方法 `identity<T>`：String/Integer 往返、类型变量 T、`toGenericString` 含 `<T>`（3+）。
4. 异常包装：`fail()` 抛 `IllegalStateException` → `InvocationTargetException`，cause 精确类型+消息、`getTargetException()==getCause()`。
5. 重载：`pick(int)`/`pick(String)` 精确定位 + `equals` 区分。
6. 变长参数：`sum(int...)` 打包数组/空数组调用、`variadic(int,String...)` 尾参数组、源码展开形式必须 `IllegalArgumentException`（JDK-27 行为）。
7. 跨 nest 私有方法：`MethodSeparate.secret` 先 `IllegalAccessException`，`setAccessible(true)` 后正常返回。
8. 参数错配：数量/类型/receiver 类型错 `IllegalArgumentException`，实例方法 null receiver `NullPointerException`。
9. 桥方法：`Comparable<StringCmp>` 的 `compareTo(Object)` isBridge+isSynthetic 且可 invoke；原方法两者皆否。
10. `isDefault`（接口 default 真、类方法假）与 `getDefaultValue`（注解默认值 "dflt"、普通方法 null）。
11. `equals/hashCode` 值语义、`toString` 含签名。
白盒部分（`testWhiteBox`）保留原结构断言 + final/超类检查。产出：`MethodTest.java`（59 断言通过）。
