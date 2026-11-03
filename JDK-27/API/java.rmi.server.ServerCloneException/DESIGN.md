# java.rmi.server.ServerCloneException 行为测试设计

## 1. API 功能
- 构造器：`ServerCloneException(String s)`、`ServerCloneException(String s, Exception cause)`。
- 继承：`extends CloneNotSupportedException`（非 IOException/RemoteException）。
- 成员：公开 `Exception detail` 字段、`getMessage()`、`getCause()`（返回 `detail`）。
- 约束：构造时 `initCause(null)` 禁止后续 `initCause`。

## 2. Java 用户如何使用
- 远程对象 `clone()` 失败时由 `UnicastRemoteObject.clone()` 包装为 `ServerCloneException("Clone failed", e)`。
- 调用方 `catch (CloneNotSupportedException e)`，用 `e.getCause()` 取真正原因。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/UnicastRemoteObject.java`：`clone()` 捕获 `RemoteException` 后 `throw new ServerCloneException("Clone failed", e)`。
- JDK 测试 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java`：对 `new ServerCloneException(foo, t)` 断言 `detail` 与 `getCause()` 都等于 `t`。
- 该类为 JDK1.4 异常链改造的典型实例。

## 4. 测试用例设计 + 自动化实现
- 白盒：父类为 `CloneNotSupportedException`、public、具体类。
- 黑盒：单参 message/`getCause()`/`detail`；双参 cause 同一性 + message 前缀 + `nested exception` 标记 + 含 cause 文本；`initCause` 抛 `IllegalStateException`。
- 实现见 `ServerCloneExceptionTest.java`。
