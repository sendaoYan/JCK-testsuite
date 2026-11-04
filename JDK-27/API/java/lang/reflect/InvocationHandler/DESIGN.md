# java.lang.reflect.InvocationHandler — 测试设计（JDK-27）

## 1. API 功能
`InvocationHandler` 函数式接口（单抽象方法 `invoke(Object proxy, Method method, Object[] args) throws Throwable`）：
- 分派处理：接收代理实例、被调 `Method`、装箱参数数组（无参方法 args 为 null）。
- 静态方法 `invokeDefault(Object proxy, Method method, Object... args)`（JDK-16+）：在 handler 内执行接口 default 方法真实现；非代理实例/非 default 方法抛 `IllegalArgumentException`。

## 2. Java 用户如何使用
实现（匿名类或 lambda）`invoke` → 按 `method.getName()/getReturnType()` 决定返回或抛异常 → 交给 `Proxy.newProxyInstance`。AOP 记录、懒加载、权限检查、RPC 转发均写在此处；需要保底逻辑时调用 `invokeDefault`。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Proxy/DefaultMethods.java`：`invokeDefault` 执行 default 体与非法目标异常。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/InvocationHandler.java`：args 形态与 invokeDefault 的参数校验权威实现。
- Spring `JdkDynamicAopProxy.invoke`（spring-aop）：拦截 `Method` + args 链式处理（同构场景 1/5）。
- MyBatis `MapperProxy.invoke`：按 method 分派 SQL 命令（场景 1）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`InvocationHandlerTest.testBlackBox`，`Recorder` 捕获 (proxy, method, args) 三元组）：
1. 三元组精确性：proxy 即活代理、method 名/声明类/返回类型、args 装箱有序；按 `getReturnType` 定向应答。
2. lambda 实现：单参记录；Object 方法 args 形态（hashCode/toString null、equals 1 参）。
3. `invokeDefault`：default 方法执行真实现、非 default 仍走 handler、抽象方法与非代理实例 → `IllegalArgumentException`。
4. handler 抛运行时异常原样冒泡。
5. 同一 handler 实例跨代理/跨接口复用：计数与“最后一次分派”状态。
白盒保留原有分派断言 + 接口/单抽象方法/static invokeDefault 检查。产出：`InvocationHandlerTest.java`（23 断言通过）。
