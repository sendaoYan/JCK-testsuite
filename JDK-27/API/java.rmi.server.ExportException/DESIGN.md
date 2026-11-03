# java.rmi.server.ExportException 行为测试设计

## 1. API 功能
- 构造器：`ExportException(String s)`、`ExportException(String s, Exception ex)`。
- 继承：`ExportException extends java.rmi.RemoteException extends java.io.IOException`。
- 成员：继承的公开 `Throwable detail` 字段、`getMessage()`、`getCause()`（由 RemoteException 重写，返回 `detail`）。
- 约束：RemoteException 在构造时调用 `initCause(null)` 以禁止后续 `initCause`。

## 2. Java 用户如何使用
- `throw new ExportException("导出失败")`；捕获 `IOException` 或 `RemoteException`。
- `new ExportException("msg", cause)` 携带根因；`e.getCause()` 取根因、`e.detail` 为兼容旧字段。
- `e.getMessage()` 对双参构造返回 `"msg; nested exception is: \n\t" + detail`。

## 3. 开源代码实际用法调研
- JDK 自身（`src/java.rmi/share/classes/java/rmi/RemoteException.java`）：`getCause()` 返回 `detail`，`getMessage()` 拼接 `nested exception`。
- JDK 测试 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java`：断言异常链中 `detail` 字段与 `getCause()` 指向同一 `cause`。
- RMI 运行时 `sun.rmi.transport.*` 在导出失败时 `throw new ExportException(...)`。

## 4. 测试用例设计 + 自动化实现
- 白盒：类可加载、public、非接口/枚举/注解、父类为 `RemoteException`、是 `IOException`。
- 黑盒：单参构造 message/`getCause()`/`detail`；双参构造 cause 同一性 + message 前缀 + `nested exception` 标记 + 含 cause 文本；`initCause` 抛 `IllegalStateException`。
- 实现见 `ExportExceptionTest.java`（`testWhiteBox`/`testBlackBox`）。
