# java.lang.reflect.Executable — 测试设计（JDK-8）

## 1. API 功能

`Executable` 是 `Method` 与 `Constructor` 的公共抽象超类（JDK-8 引入）：
- 参数视图：`getParameterTypes/getParameterCount/getParameters/getGenericParameterTypes/getAnnotatedParameterTypes/getParameterAnnotations`。
- 异常视图：`getExceptionTypes/getGenericExceptionTypes/getAnnotatedExceptionTypes`。
- 声明：`getTypeParameters/getDeclaringClass/getName/getModifiers/isVarArgs/isSynthetic/toGenericString/getAnnotatedReturnType/getAnnotatedReceiverType`。
- 继承 `AccessibleObject`（可 `setAccessible/isAccessible`）并实现 `Member + GenericDeclaration`。
- JDK-8 无 `accessFlags()`（JDK 20 才加入）。

## 2. Java 用户如何使用

以 `Executable` 统一处理方法与构造器：框架参数绑定/校验（Spring `MethodParameter`、
Hibernate Validator `ExecutableParameterNameProvider`）、文档/签名渲染、AOP 切点解析。
`isDefault` 仅在 `Method`、`newInstance` 仅在 `Constructor`，基类承载两者共有查询。

## 3. 参考来源（只列路径）

- Oracle Java SE 8 javadoc：`java/lang/reflect/Executable.html`。
- OpenJDK 8 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Executable.java`。
- OpenJDK 回归测试：`~/git/jdk8u-dev/jdk/test/java/lang/reflect/`、`~/git/jdk/test/jdk/java/lang/reflect/Parameter/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/Executable/`。

## 4. 测试用例设计（ExecutableTest.java，12 → 57 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 保留原结构断言（Sample.m/varargs 的计数/类型/修饰符） | testExecutable |
| P0 | Method+Constructor 双证：参数对象长度/类型对齐/反向引用、注解返回类型（方法 int、构造器=声明类）、注解参数 | testDualEvidence |
| P0 | 类型参数视图：非泛型空、泛型 T 界 Number、参数 TypeVariable、GenericDeclaration 关系 | testTypeParametersView |
| P0 | 异常视图：IOException→ClassNotFoundException 声明顺序、泛型视图、AnnotatedExceptionTypes 类型 | testExceptionTypesView |
| P0 | 注解矩阵：参数注解行/空行、构造器无声明注解、缺失注解查询 | testAnnotationMatrix |
| P0 | accessible 属性联动：单对象翻转独立、批量 setAccessible(AccessibleObject[]) | testAccessibleFlagLinkage |
| P0 | 接收者证据：实例方法非 null、静态方法 null、内部类构造器=外围类、**静态嵌套构造器 JDK-8 非空退化** | testReceiverTypeEvidence |
| P1 | Member 契约（名字/声明类/修饰符/isSynthetic/构造器名==类名） | testMemberContract |
| P1 | varargs 一致性（Executable 标志 + Parameter 标志 + 数组类型）、toGenericString 片段 | testVarargsConsistency |
| P1 | JDK-8 API 面锁定：`Executable.class.getMethod("accessFlags")` → NSME | testApiSurfaceLocked |

## 易错规范细节（已锁入断言）

- `getAnnotatedReceiverType()`：静态方法两侧均 null；静态嵌套类构造器 JDK-8 返回**非空退化值**
  （getType()==外围类，JDK-8044629，JDK-9 修正为 null）；非静态内部类构造器两侧均非空。
- `getExceptionTypes()` 按声明顺序返回，不做子类合并。
- varargs 标志在 `Executable.isVarArgs()` 与末位 `Parameter.isVarArgs()` 两级一致。
- `accessible` 标志是每个反射对象的独立状态，批量 API 逐元素翻转。
