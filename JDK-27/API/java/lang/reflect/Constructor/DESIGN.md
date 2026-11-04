# java.lang.reflect.Constructor 测试设计案（JDK-27）

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

## JDK-27 差异（已锁入断言）

- 私有构造器 IAE 路径改用独立顶层类 `ExtCtor`（非 nestmate）。
- 静态嵌套类构造器 `getAnnotatedReceiverType()` 返回 null（JDK-8 返回非 null 退化值）；非静态内部类
  构造器返回非 null 且 receiver 类型为外围类（`InnerCtor` 需以 `(外围类,long,String)` 取构造器）。

## 深化批次（2026-11-05）

新增 8 个 test 方法（59 → 96 断言），与 JDK-8 侧同构（见该侧 DESIGN 的批次表），差异点：

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | newInstance 精确类型/字段往返；"argument type mismatch" 与 varargs 散参被拒消息 | testNewInstanceExactType / testNewInstanceMismatchMessages |
| P0 | enum 反射创建拒绝 "Cannot reflectively create enum objects" | testEnumNewInstance |
| P0 | 泛型抛异常构造器（E 界/擦除/TypeVariable） | testGenericExceptionConstructor |
| P0 | 接收者证据：内部类构造器 receiver==外围类、静态嵌套构造器 **null**（JDK-9+ 修正后行为） | testReceiverTypeByKind |
| P1 | 接口构造器缺失 / hashCode 公式与 NSME "<init>" 消息 / toString 签名片段 | testInterfaceConstructorAbsence / testHashCodeFormulaAndLookup / testToStringForms |

### 锁定的版本差异（本批实测）
- `getAnnotatedReceiverType()` 静态嵌套构造器：JDK-27 返回 null（JDK-8 为非空退化值）。
- varargs `Constructor.newInstance` 与 `Method.invoke` 一样**不做散参打包**（两侧一致）：参数个数必须等于形参个数。
