# java.lang.reflect.UndeclaredThrowableException — 测试设计（JDK-27）

## 1. API 功能
`UndeclaredThrowableException extends RuntimeException`：
- 构造器：`(Throwable undeclaredThrowable)`、`(Throwable, String)`。
- `getUndeclaredThrowable()` 与 `getCause()`（同一引用）。
- 触发方：动态代理 handler 抛出「接口方法未声明的受检异常」时由代理包装抛出；运行时异常/错误与已声明受检异常原样传播。

## 2. Java 用户如何使用
调用代理对象时 catch UTE → `getUndeclaredThrowable()`（或 `getCause()`）取真实根因处理。RPC 存根/AOP 面板中最常见。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Proxy/Basic1.java` 系列：handler 抛出受检异常的包装路径。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/UndeclaredThrowableException.java` 与 `Proxy` 内的分派实现。
- Spring AOP `UndeclaredThrowableException` 同名处理的透传链路；MyBatis/Dubbo 代理层 catch 解包（同构场景 1/4）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`UndeclaredThrowableExceptionTest.testBlackBox`，接口 `Safe`/`Declared` + 真实代理触发）：
1. 真实触发：未声明受检（IOException）→ UTE，`getUndeclaredThrowable/getCause` 与原异常同引用。
2. 已声明受检异常原样传播（不包装，负例双分支）。
3. 运行时异常原样传播。
4. 构造器面：带消息/裸构造（消息 null、按引用、toString）。
5. 二次分派仍包装同一引用（确定性）。
6. `Error` 类型原样传播。
白盒保留原有构造器断言 + 继承层次。产出：`UndeclaredThrowableExceptionTest.java`（16 断言通过）。
