# java.rmi.server.SocketSecurityException 行为测试设计

## 1. API 功能
- 构造器：`SocketSecurityException(String s)`、`SocketSecurityException(String s, Exception ex)`。
- 继承：`extends java.rmi.server.ExportException`（进而 extends RemoteException/IOException）。
- 语义：导出远程对象时 socket 创建被 SecurityManager 拒绝。

## 2. Java 用户如何使用
- 导出远程对象失败时可能捕获到该类（作为 `ExportException`/`IOException` 的子类）。
- 双参构造携带根因，`getCause()`/`detail` 指向 `ex`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/SocketSecurityException.java`：继承 `ExportException`，两构造器。
- `sun.rmi.transport.tcp.TCPTransport` 在 socket 权限校验失败路径抛出此类。
- JDK 测试 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java` 覆盖异常链通用行为。

## 4. 测试用例设计 + 自动化实现
- 白盒：父类为 `ExportException`、是 `IOException`、public、具体类。
- 黑盒：单参 message + cause null；双参 cause 同一性 + message 前缀 + `nested exception` + 含 cause 文本。
- 实现见 `SocketSecurityExceptionTest.java`。
