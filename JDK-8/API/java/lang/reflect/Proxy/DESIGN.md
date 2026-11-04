# java.lang.reflect.Proxy 测试设计案（JDK-8）

> 覆盖目标：完整契约。newProxyInstance 分发每个方法（含 Object 的 hashCode/equals/toString）到 handler
> 且携带 (proxy,method,args)、isProxyClass 判断、getInvocationHandler 身份 + 非代理→IAE、handler 返回错类型→CCE、
> 基本类型返回 null→NPE、运行时异常原样传播、未声明受检异常→UndeclaredThrowableException、工厂异常矩阵。

## 1. API 功能

- `newProxyInstance(loader, interfaces[], handler)`、`getProxyClass`、`isProxyClass`、`getInvocationHandler`。
- 生成的代理类实现接口 + Serializable，所有方法委托给 InvocationHandler。

## 2. 用户如何使用

- 动态实现接口：`(Foo) Proxy.newProxyInstance(loader, new Class[]{Foo.class}, h)`。
- `Proxy.isProxyClass(c)` 判断某 Class 是否代理类；`getInvocationHandler(proxy)` 取 handler。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/Proxy.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Proxy.java`。
- JCK runtime-8b `tests/api/java_lang/reflect/Proxy/`。

## 4. 测试用例设计（映射到 ProxyTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 分发（含 Object 三方法）| testDispatch |
| P0 | isProxyClass/getInvocationHandler 身份 | testIdentity |
| P0 | 多接口代理 | testMultipleInterfaces |
| P0 | handler 返回行为（CCE/NPE/运行时异常/UndeclaredThrowableException）| testHandlerReturnBehavior |
| P0 | 工厂异常矩阵 | testFactoryExceptions |

## 易错规范细节（已锁入断言）

- `newProxyInstance(null, ifaces, h)` → IAE（非 NPE）；null interfaces/handler → NPE。
- 空接口数组合法（生成无接口代理）；重复接口/非接口/数组接口 → IAE；null 接口元素 → NPE。
- handler 返回错类型 → ClassCastException；基本类型返回 null → NPE。
- handler 抛未声明受检异常 → UndeclaredThrowableException（cause 保留）。

## 深化批次（2026-11-05）

新增方法（在原有 5 个方法上追加，断言 43 → 127）：

| 测试点 | 方法 |
|---|---|
| 参数形态：无参方法 args==null、varargs 尾参数组（空为长度 0 数组非 null）、long/double/boolean 装箱 | testArgsShapes |
| Object 方法路由：toString/hashCode/equals 全走 handler 且 declaringClass==Object.class、equals(null) args=[null]、调用顺序记录 | testObjectMethodRouting |
| default 方法路由：dm() 进 handler、Method.isDefault() 真、声明类为接口 | testDefaultMethodRouting |
| 方法冲突解析：同签名双接口（解析到首个接口）、继承方法声明类为父接口、父接口+子接口组合 | testMethodConflictResolution |
| 代理类形态：父类==Proxy、final、非接口/非 synthetic、包私有接口→包级代理、public 接口→public final 代理（8: com.sun.proxy.$ProxyN）、getProxyClass 同类、同类复用 | testProxyClassShape |
| 可见性与自定义 loader：手写 v52 接口字节码经子 loader 定义 → app loader 视角 IAE "is not visible from class loader"、子 loader 生成 OK、代理类定义在请求 loader、双亲链 | testVisibilityAndCustomLoader |
| bootstrap loader：null loader + Supplier.class（对 bootstrap 可见）→ 代理可用、getProxyClass(null) 同类 | testBootstrapLoader |
| 异常矩阵深化：未声明受检 → UTE cause 同一对象且 message==null、声明受检原样、Error 原样 | testUndeclaredThrowableDeep |
| 代理序列化 round-trip：Serializable 代理 + Serializable handler 经 ByteArrayOutputStream 往返仍可分发 | testProxySerialization |
| 工厂消息片段："not a proxy instance"/"repeated interface"/"is not an interface"/"is not visible from class loader"/CCE "cannot be cast to" | testFactoryMessages |

### 版本差异（实测锁定）

- 公有接口代理类名：JDK-8 `com.sun.proxy.$ProxyN`，JDK-27 动态模块 `jdk.proxyK.$ProxyM`；断言取公共片段 `contains("$Proxy")`。
- null loader + 不可见接口的 IAE 消息措辞不同（8: "interface X is not visible from class loader"；27: "X referenced from a method is not visible from class loader: null"），断言取公共子串 "is not visible from class loader"。
- CCE 消息 27 带模块后缀，公共子串 "cannot be cast to"。
- proxy 生成 UTE 的 message 两侧均为 null；getCause()/getUndeclaredThrowable() 与 handler 抛出的对象同一。
- isProxyClass(null) 两侧均 NPE（27 由内部 WeakCache.requireNonNull 路径保证）。
