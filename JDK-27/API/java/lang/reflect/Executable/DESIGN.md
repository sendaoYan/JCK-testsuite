# java.lang.reflect.Executable — 测试设计（JDK-27）

## 1. API 功能
`Executable` 是 `Method` 与 `Constructor` 的公共抽象超类：
- 参数视图：`getParameterTypes/getParameterCount/getParameters/getGenericParameterTypes/getAnnotatedParameterTypes/getParameterAnnotations`。
- 异常视图：`getExceptionTypes/getGenericExceptionTypes/getAnnotatedExceptionTypes`。
- 声明：`getTypeParameters/getDeclaringClass/getName/getModifiers/accessFlags/isVarArgs/isSynthetic/toGenericString/getAnnotatedReturnType/getAnnotatedReceiverType`。
- 继承 `AccessibleObject` 并实现 `Member + GenericDeclaration`。

## 2. Java 用户如何使用
以 `Executable` 统一处理方法与构造器：框架参数绑定/校验（Spring `MethodParameter`、Hibernate Validator）、文档生成器（签名渲染）。注意 `isDefault` 仅在 `Method`、`newInstance` 仅在 `Constructor`，基类提供两者共有的全部查询。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Parameter/`（以 `Executable.getParameters` 为入口）。
- JDK 自测 `AccessFlag/MethodAccessFlagTest.java`：`Executable.accessFlags()`。
- Spring `MethodParameter(Executable executable, int index)`（spring-core）：以 Executable 为统一构造入口（同构场景 1/3/4）。
- Hibernate Validator `ExecutableParameterNameProvider`：`getParameters` 驱动参数校验命名。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ExecutableTest.testBlackBox`，固定类 `Rich`（泛型方法+双构造器）/`WithDefault`）：
1. 泛型方法：类型变量 T 及界、泛型参数类型、注解返回/参数类型保 TV、`toGenericString` 含 `<T extends java.lang.Number>`。
2. 异常视图：声明异常精确两类 + 泛型形态。
3. 构造器同契约：名为类名、参数视图、varargs 标志（Executable/Parameter 两级）、注解返回为声明类。
4. 参数对象与 `accessFlags` 精确集合（方法 PUBLIC 唯一、构造器含 PUBLIC）。
5. 参数注解矩阵行数与空注解。
6. default（经 Method 子类型）与 static 标志。
7. `Executable` 视图与 `Method` 值等价。
白盒保留原有方法/构造器结构断言 + 抽象类层次。产出：`ExecutableTest.java`（40 断言通过）。
