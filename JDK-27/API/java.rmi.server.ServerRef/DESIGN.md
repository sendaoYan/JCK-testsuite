# java.rmi.server.ServerRef 行为测试设计

## 1. API 功能
- 接口，`extends RemoteRef`，常量 `serialVersionUID`。
- 新增方法：`RemoteStub exportObject(Remote obj, Object data)`、`String getClientHost()`。
- 表示服务端引用，负责导出与客户端主机解析。

## 2. Java 用户如何使用
- RMI 运行时（`UnicastServerRef` 等）实现该接口以导出远程对象。
- `getClientHost()` 返回当前远程调用的客户端主机。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/ServerRef.java`：extends RemoteRef，两方法。
- JDK 源码 `sun.rmi.server.UnicastServerRef`：核心 ServerRef 实现。
- `RemoteServer.getClientHost()` 最终委托到具体 ServerRef 的传输层。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、extends RemoteRef、`getClientHost`/`exportObject` 签名。
- 黑盒：匿名实现 RemoteRef+ServerRef 全部方法；`getClientHost`/`exportObject` 哨兵返回；`instanceof RemoteRef`；继承的 `remoteToString`。
- 实现见 `ServerRefTest.java`。
