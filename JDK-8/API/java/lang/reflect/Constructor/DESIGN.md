# java.lang.reflect.Constructor 测试设计案（JDK-8）

> 覆盖目标：完整契约。参数/异常类型反射（含泛型类型参数）、newInstance 成功 + 错参数→IAE、私有访问控制、
> 抽象/接口→InstantiationException、InvocationTargetException 包装、isVarArgs/isSynthetic、注解矩阵、
> equals/hashCode/toString/toGenericString、getConstructors vs getDeclaredConstructors、NSME/NPE 路径。

## 1. API 功能

- 元数据：getParameterTypes/getExceptionTypes/getGeneric*/getTypeParameters/getParameterCount/getModifiers。
- 实例化：`newInstance(args...)`；varargs/synthetic 判断。
- 注解：getAnnotation/getDeclaredAnnotations/getParameterAnnotations/getAnnotatedReturnType/getAnnotatedReceiverType。

## 2. 用户如何使用

- `C.class.getConstructor(paramTypes...)` 取 public 构造器；`getDeclaredConstructor` 取声明构造器。
- `ctor.newInstance(args)` 反射构造对象；构造器抛出的受检异常被包进 InvocationTargetException。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/Constructor.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Constructor.java`。
- JCK runtime-8b `tests/api/java_lang/reflect/Constructor/`。

## 4. 测试用例设计（映射到 ConstructorTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 元数据/泛型构造器类型参数 | testMetadata / testGenericConstructor |
| P0 | newInstance 成功 + 错参数矩阵 | testNewInstance |
| P0 | 私有构造器访问控制 | testAccessibility |
| P0 | 抽象类 InstantiationException | testInstantiationException |
| P0 | InvocationTargetException 包装 | testInvocationTargetException |
| P0 | varargs 构造器 | testVarArgs |
| P1 | 注解矩阵 | testAnnotations |
| P1 | equals/hashCode/toString/toGenericString | testEqualsHashCodeToString |
| P1 | getConstructors/getDeclaredConstructors | testDiscovery |

## 易错规范细节（已锁入断言）

- 公共构造器数 `getConstructors()==6`，声明构造器数 `getDeclaredConstructors()==7`（含私有）。
- `getConstructor((Class[])null)` → 无参构造器；`getConstructor(int.class,null)` → NSME。
- 抽象类 `newInstance()` → InstantiationException。
