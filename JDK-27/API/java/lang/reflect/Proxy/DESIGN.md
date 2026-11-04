# java.lang.reflect.Proxy — 测试设计（JDK-27）

## 1. API 功能
`Proxy` 运行时生成实现一组接口的代理类：
- 创建：`newProxyInstance(ClassLoader, Class<?>[], InvocationHandler)`、`getProxyClass(ClassLoader, Class<?>...)`（等价于前者触发的类生成）、protected 构造器由生成类使用。
- 查询：`isProxyClass(Class<?>)`、`getInvocationHandler(Object)`。
- 行为约定：所有接口方法（含 `java.lang.Object` 的 `hashCode/toString/equals`）分派到 handler；default 方法可经 `InvocationHandler.invokeDefault` 执行真实现；未声明的受检异常包装为 `UndeclaredThrowableException`；返回值与接口返回类型不符在调用点 `ClassCastException`/拆箱 `NullPointerException`。
- 校验：非接口/重复接口 → `IllegalArgumentException`；null handler → `NullPointerException`。

## 2. Java 用户如何使用
定义业务接口 + `InvocationHandler`（记录/鉴权/懒加载/RPC 转发）→ `newProxyInstance` → 以接口类型调用 → handler 内检查 `Method`/args 决定行为；需要 default 逻辑时 `invokeDefault`。AOP（Spring JDK 代理）、MyBatis Mapper、RMI/Dubbo 存根皆此模型。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Proxy/Basic1.java`：`newProxyInstance` + 分派返回值断言。
- JDK 自测 `Proxy/DefaultMethods.java`：default 方法经 `invokeDefault` 的行为（JDK-16+ API）。
- JDK 自测 `Proxy/Boxing.java`/`TestVarArgs.java`：拆装箱与 varargs 分派的边界。
- Spring `JdkDynamicAopProxy`（spring-aop）：handler 记录并转发调用（对应场景 1/2）；MyBatis `MapperProxy`：接口方法 → SQL 命令分派（场景 1）。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/Proxy.java`：异常包装规则权威实现。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ProxyTest.testBlackBox`，`RecordingHandler` 记录方法名+参数个数并按罐头值应答）：
1. 分派链路：答案回流、装箱 int 拆箱、调用记录（无参方法 args 为 null、有参为长度）、null 返回拆 int → `NullPointerException`。
2. `Object` 方法分派：hashCode=42、toString="ProxyStr"、equals 自反真/他值假 + 调用记录数。
3. 返回类型错配：int 方法返回 String → 调用点 `ClassCastException`。
4. 异常路径：未声明受检 → `UndeclaredThrowableException`（cause 精确 IOException）；已声明受检原样传播；运行时异常原样传播。
5. `InvocationHandler.invokeDefault`：handler 内执行真 default 体返回 "default-describe"。
6. 类缓存：同 loader+接口同代理类（与 `getProxyClass` 一致）、`isProxyClass` 正反例、实例各自持有 handler。
7. 多接口单代理 + 双接口均分派；重复接口 → `IllegalArgumentException`。
8. 参数校验：非接口 IAE、null handler NPE、`getInvocationHandler` 非代理 IAE。
白盒保留原有创建/查询断言 + Serializable/结构检查。产出：`ProxyTest.java`（37 断言通过）。
