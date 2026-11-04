# java.rmi.server.RemoteServer 行为测试设计

## 1. API 功能
- 抽象类，`extends RemoteObject`；受保护构造器。
- 静态方法：`String getClientHost()`、`setLog(OutputStream)`、`PrintStream getLog()`。
- 服务端实现的公共基类。

## 2. Java 用户如何使用
- 子类 `UnicastRemoteObject` 等继承；在远程调用线程内 `getClientHost()` 取客户端主机，否则抛 `ServerNotActiveException`。
- `setLog(out)` 打开 RMI 调用日志（null 关闭）；`getLog()` 返回日志流（默认 null）。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RemoteServer.java`：`getLog()` 返回 `logNull ? null : callLog.getPrintStream()`。
- `sun.rmi.server.UnicastServerRef` 用 `callLog` 记录 RMI 调用。
- `UnicastRemoteObject` 继承 RemoteServer，构成导出对象继承链。

## 4. 测试用例设计 + 自动化实现
- 白盒：public abstract、父类 RemoteObject。
- 黑盒：默认 `getLog()` null；`setLog`/`getLog` 往返（用后恢复 null）；`getClientHost()` 抛 ServerNotActiveException。
- 实现见 `RemoteServerTest.java`。
