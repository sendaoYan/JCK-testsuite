# java.lang.reflect.InvocationHandler — 测试设计案（JDK-8）

## 1. API 功能

- 函数式接口（JDK-8 上仅一个抽象方法）：`Object invoke(Object proxy, Method method, Object[] args) throws Throwable`。
- JDK-8 没有静态/默认成员；`invokeDefault` 是 JDK-16+ 才加入（本侧用成员普查断言锁定）。
- 接口未被标注 `@FunctionalInterface`（实测 JDK-8 与 JDK-27 均无此注解），但按形态可用 lambda 实现。

## 2. 用户如何使用

- `Proxy.newProxyInstance(loader, ifaces, handler)` 传入实现（匿名类或 lambda），所有代理方法调用都进入 `invoke`，按 `method.getName()/getReturnType()` 决定应答或抛异常。
- AOP 拦截、RPC 远端转发、懒加载代理、权限检查均落在此接口。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/InvocationHandler.html：args 无参为 null、装箱规则）。
- OpenJDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/InvocationHandler.java`。
- OpenJDK 8 回归 `~/git/jdk8u-dev/jdk/test/java/lang/reflect/Proxy/`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/Proxy/`。

## 4. 测试用例设计（映射到 InvocationHandlerTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 基本分发（保留原有） | testInvocationHandler |
| P0 | 接口契约反射：isInterface、无父接口、单抽象方法、invoke 的参数/返回/throws Throwable | testInterfaceContract |
| P0 | 成员普查：JDK-8 恰好 1 个声明方法（无 static/default） | testInterfaceContract |
| P1 | @FunctionalInterface 注解缺失（两侧一致）+ lambda 可实例化 | testInterfaceContract / testLambdaInstantiation |
| P1 | lambda 应答（null 值/精确值）、Object 方法 args 形态（null/null/1） | testLambdaInstantiation |
| P1 | handler 内可见的 proxy 事实：活代理、isProxyClass、getInvocationHandler 往返 | testProxyFactsInHandler |
| P1 | 异常中继：声明受检原样、未声明受检被 Proxy 包成 UTE、运行时异常按同一对象透传 | testExceptionRelay |

## 易错规范细节（已锁入断言）

- 无参方法 args 为 null（不是空数组）；有参方法为装箱后的 Object[]。
- 接口在 JDK-8 的 getDeclaredMethods() 恰为 1（invoke）；JDK-27 为 2（+ static invokeDefault）。
- `InvocationHandler.class.getAnnotation(FunctionalInterface.class)` 两侧均为 null —— 形态上仍是函数式接口。
- handler 抛出的 RuntimeException 按同一引用透传（不包装）。
