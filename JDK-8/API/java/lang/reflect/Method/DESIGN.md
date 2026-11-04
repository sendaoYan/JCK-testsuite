# java.lang.reflect.Method 测试设计案（JDK-8）

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

## 深化批次（2026-11-05）

新增 12 个 test 方法（81 → 153 断言），核对 Oracle Java SE 8 javadoc `Method.invoke` 全文后锁定的契约：

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | varargs 精确元数契约：散参形式被拒（IAE "wrong number of arguments"）、数组两种传法结果相等、空数组 → 0 | testInvokeVarargsExactArity |
| P0 | invoke 精确值（拼接/静态 null 接收者/接收者被忽略） | testInvokeExactValues |
| P0 | 错接收者类型 IAE 消息含 "is not an instance of"；null 装箱给 int 参数 → IAE；Long 不缩窄 | testInvokeReceiverAndNullArg |
| P0 | RuntimeException cause 同一对象（ITE.getCause()==getTargetException()==抛出的实例）；null varargs 数组透传 → ITE(NPE) | testInvokeExceptionIdentity |
| P0 | 私有未 setAccessible → IllegalAccessException 消息含 "access a member of class" + 成员类名（JDK-8 无 nestmate，嵌套私有即触发） | testAccessMessage |
| P0 | 重载三分辨（int/int,int/String）互不相等、参数个数区分 | testOverloads |
| P0 | 嵌套泛型返回类型分解：Map<String,List<Integer>> raw/实参递归/typeName 全等；pick 的 TypeVariable 及 bound | testGenericReturnTypeDeep |
| P0 | 接口 static/default/abstract 经 invoke 真调用 + 虚分派计数 | testInterfaceInvoke |
| P0 | bridge 虚分派（invoke 到子类覆写）、每个覆写子类自带 bridge、bridge 与真实方法共享按名字 hash | testBridgeDispatch |
| P1 | getDefaultValue 四种类型（Class/int[]/enum/String[]）内容精确 | testDefaultValueKinds |
| P1 | hashCode 文档公式 `declaringClass.getName().hashCode() ^ name.hashCode()`；getMethod==getDeclaredMethod；NSME 消息含 "nonexistent()" | testStringFormsAndHashFormula |
| P1 | AnnotatedType 家族：返回/参数/异常/接收者（实例非 null、静态 null） | testAnnotatedTypeFamily |

### 锁定的 JDK-8 vs JDK-27 差异（本批实测）
- `toGenericString()`：JDK-8 打印 `<T>`（省略类型参数界），JDK-9+ 打印 `<T extends java.lang.Number>`——两侧各自断言。
- 异常消息：IAE 元数错误 JDK-8 为 `wrong number of arguments`，JDK-27 为 `wrong number of arguments: N expected: M`（断言公共子串）；错接收者 JDK-8 为 `object is not an instance of declaring class`，JDK-27 为 `object of type X is not an instance of Y`（断言公共子串）。
- 嵌套类私有访问：JDK-8（无 nestmate）反射拒绝（本文件用它做负例）；JDK-27 允许，负例需非同巢类。
