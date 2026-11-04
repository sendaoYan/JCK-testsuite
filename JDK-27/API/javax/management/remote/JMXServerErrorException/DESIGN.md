# JMXServerErrorException 测试设计

## 1. API 功能
`javax.management.remote.JMXServerErrorException`（继承 `java.io.IOException`）表示 JMX 服务器端抛出 `Error`，对应 RMI 的 `ServerError`。

- 构造器：`JMXServerErrorException(String message, Error err)`（仅此一个）；`err` 可为 null（标准连接器不会传 null）。
- 查询：`getCause()` 返回构造时的 `Error`（覆盖 `Throwable.getCause()`）。

## 2. Java 用户如何使用
```java
throw new JMXServerErrorException("server failed", new Error("boom"));
// 调用方 catch (JMXServerErrorException e) 后读 e.getCause() 得到原始 Error
```
典型组合链路：`RMIConnection`/`RMIServer` 实现捕获服务器端 `Error` 后包装为 `JMXServerErrorException` 传回客户端。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnectionImpl.java` 与 `RMIServerImpl.java`：处理 RMI 调用时把服务器端 `Error` 包装为 `JMXServerErrorException` 抛回。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXServerErrorException.java`：`getCause()` 返回 `final Error cause` 字段。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
黑盒部分（`JMXServerErrorExceptionTest.testBlackBox()`）：
- 正常路径：message 与同一 `Error` 实例保留、`getCause() instanceof Error`、`instanceof IOException`。
- 边界：`err` 传 null 时 message 保留且 `getCause()` 为 null。
