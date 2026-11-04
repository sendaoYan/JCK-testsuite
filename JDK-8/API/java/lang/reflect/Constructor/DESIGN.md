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

## 深化批次（2026-11-05）

新增 8 个 test 方法（57 → 95 断言）：

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | newInstance 精确运行时类型 + 字段值往返 | testNewInstanceExactType |
| P0 | int 构造器传 "x" → IAE "argument type mismatch"；null 给 int 参数 → IAE；**varargs 构造器散参形式被拒**（"wrong number of arguments"）、两数组形式结果相等、空数组 | testNewInstanceMismatchMessages |
| P0 | enum 反射实例化被拒：构造器擦除为 (String,int)、非 synthetic、setAccessible 后仍 IAE "Cannot reflectively create enum objects" | testEnumNewInstance |
| P0 | 泛型抛异常构造器 `<E extends Exception>`：getExceptionTypes 擦除 Exception、getGenericExceptionTypes[0] 是 TypeVariable、E 界 | testGenericExceptionConstructor |
| P0 | 接收者类型证据：非静态内部类构造器 receiver==外围类；静态嵌套构造器 **JDK-8 返回非空退化值**（JDK-9 起为 null） | testReceiverTypeByKind |
| P1 | 接口无构造器：getConstructors/getDeclaredConstructors 均空、查找 NSME | testInterfaceConstructorAbsence |
| P1 | hashCode 文档公式 == 声明类名 hash；NSME 消息含 "<init>" 与参数类型 | testHashCodeFormulaAndLookup |
| P1 | toString/toGenericString 签名片段；泛型构造器 `<T>` | testToStringForms |

### 锁定的 JDK-8 vs JDK-27 差异（本批实测）
- `getAnnotatedReceiverType()` 对静态嵌套/顶层类构造器：JDK-8 返回非空退化 AnnotatedType（getType()==外围类，规范缺陷 JDK-8044629），JDK-9+ 返回 null；非静态内部类构造器两侧均非空且 type==外围类。
- IAE 元数错误消息：JDK-8 无计数详情，JDK-27 带 ": N expected: M"（断言公共子串）。
