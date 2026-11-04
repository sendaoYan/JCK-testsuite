# java.lang.reflect.Method 测试设计案（JDK-27）

> 覆盖目标：完整契约。返回/参数/异常类型反射（含泛型签名与类型参数）、invoke 实例/静态 + null 语义、
> 错参数量/类型→IAE、私有访问控制、InvocationTargetException 包装原始异常（cause/targetException 同源）、
> isVarArgs/isBridge/isSynthetic/isDefault、注解元素 getDefaultValue、注解矩阵、equals/hashCode/toString、
> getMethods vs getDeclaredMethods 继承语义与 NSME/NPE 路径。

## 1. API 功能

- 元数据：getReturnType/getParameterTypes/getExceptionTypes/getGeneric*/getTypeParameters/getParameterCount。
- 调用：`invoke(receiver, args...)`；varargs/bridge/default 判断。
- 注解：getDefaultValue/getAnnotation/getDeclaredAnnotations/getParameterAnnotations/getAnnotatedReturnType。

## 2. 用户如何使用

- `C.class.getMethod(name, paramTypes...)` 按签名取 public 方法（含继承）。
- `m.invoke(obj, args)` 反射调用；受检异常被包进 InvocationTargetException。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/Method.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Method.java`。
- JCK runtime-8b `tests/api/java_lang/reflect/Method/`。

## 4. 测试用例设计（映射到 MethodTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 元数据/泛型签名/类型参数 | testMetadata / testGenericSignature |
| P0 | 异常类型反射（顺序）| testExceptionTypes |
| P0 | invoke 实例/静态 + 错参数/null | testInvoke |
| P0 | InvocationTargetException cause/targetException 同源 | testInvocationTargetException |
| P0 | 私有方法访问控制 | testAccessibility |
| P0 | varargs/bridge/synthetic/default | testVarArgs / testBridgeSynthetic / testDefaultMethod |
| P0 | 注解元素 getDefaultValue | testAnnotationDefaultValue |
| P1 | 注解矩阵 | testAnnotations |
| P1 | equals/hashCode/toString/toGenericString | testEqualsHashCodeToString |
| P1 | getMethods/getDeclaredMethods 继承语义 | testDiscovery |

## 易错规范细节（已锁入断言）

- `getMethod("add",(Class[])null)` → NSME（null 被当作空数组），非 NPE。
- bridge 方法 `isBridge()` 且 `isSynthetic()`，参数/返回为擦除后 Object。
- `InvocationTargetException.getTargetException() == getCause()` 恒等。
- 实例方法 `invoke(null,..)` → NPE；错参数类型/数量 → IAE。

## JDK-27 差异（已锁入断言）

- JDK-11+ nestmates 使私有成员可被同 nest 反射访问而不需 setAccessible；IAE 路径改用独立顶层类 `ExtMethod`。

## 深化批次（2026-11-05）

新增 12 个 test 方法（81 → 152 断言），与 JDK-8 侧同构（见该侧 DESIGN 的批次表），差异点：

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | varargs 精确元数契约（散参被拒、两数组形式相等、空数组 → 0） | testInvokeVarargsExactArity |
| P0 | invoke 精确值 / 错接收者与错参数 IAE 消息片段 | testInvokeExactValues / testInvokeReceiverAndNullArg |
| P0 | RuntimeException cause 同一对象 + null varargs 数组 → ITE(NPE) | testInvokeExceptionIdentity |
| P0 | 私有未 setAccessible → IAE 消息 "access a member of class"（需非同巢 ExtMethod：JDK-11+ nestmate 允许同巢私有） | testAccessMessage |
| P0 | 重载分辨 / 嵌套泛型返回分解 / 接口三分法真调用 / bridge 虚分派 | testOverloads / testGenericReturnTypeDeep / testInterfaceInvoke / testBridgeDispatch |
| P1 | getDefaultValue 四类型 / hashCode 公式与 NSME 消息 / AnnotatedType 家族 | testDefaultValueKinds / testStringFormsAndHashFormula / testAnnotatedTypeFamily |

### 锁定的版本差异（本批实测）
- `toGenericString()` 打印类型参数界 `<T extends java.lang.Number>`（JDK-8 省略界）。
- 嵌套类私有成员经 nestmate 规则可反射访问，拒绝路径必须用非同巢类。
- IAE 元数/接收者消息更详细（带数量与类型名），断言取两侧公共子串。
