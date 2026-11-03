# java.rmi.server.ServerNotActiveException 行为测试设计

## 1. API 功能
- 构造器：`ServerNotActiveException()`、`ServerNotActiveException(String s)`。
- 继承：`extends Exception`（与 RemoteException 是兄弟关系，非其子类）。
- 语义：`RemoteServer.getClientHost()` 在当前线程无远程调用时抛出该异常。

## 2. Java 用户如何使用
- 在服务端回调中调用 `RemoteServer.getClientHost()`；若不在远程调用线程内则捕获 `ServerNotActiveException`。
- `new ServerNotActiveException()` 无消息；`new ServerNotActiveException("msg")` 带消息。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RemoteServer.java`：`getClientHost()` 委托 `TCPTransport.getClientHost()`，无活动调用时抛 `ServerNotActiveException`。
- `sun.rmi.transport.tcp.TCPTransport` 维护线程局部调用上下文，用于判断是否在调用内。
- 无第三方直接构造该异常（一般由 RMI 运行时抛出）。

## 4. 测试用例设计 + 自动化实现
- 白盒：父类为 `Exception`、public、具体类。
- 黑盒：默认构造 message 为 null；String 构造 message 回显；cause 为 null；`instanceof Exception`；精确类型为 `ServerNotActiveException`。
- 实现见 `ServerNotActiveExceptionTest.java`。
