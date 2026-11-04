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
