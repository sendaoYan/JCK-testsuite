# RMIConnectionImpl 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnectionImpl`（实现 `RMIConnection`、`java.rmi.server.Unreferenced`）是服务器端 RMI 连接的实现，把远程调用转发到本地 `MBeanServer`。

- 构造器：`(RMIServerImpl rmiServer, String connectionId, ClassLoader defaultClassLoader, Subject subject, Map<String,?> env)`。
- 方法：`getConnectionId()`、`close()`、`unreferenced()`、全套 `RMIConnection` 操作、`fetchNotifications`、`toString()`。

## 2. Java 用户如何使用
该实现通常不由用户直接构造：`RMIServerImpl.makeClient` 内部创建，经 RMI 导出后作为 `RMIConnection` stub 返回给客户端。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIServerImpl.java`：`makeClient`（抽象）由 `RMIJRMPServerImpl` 实现为 `new RMIConnectionImpl(...)`。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIJRMPServerImpl.java`：`makeClient` 实例化 `RMIConnectionImpl`。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：具体类（非抽象）、实现 `RMIConnection` 与 `Unreferenced`、构造器签名存在、关键方法存在（`getConnectionId/close/createMBean/getAttribute/fetchNotifications/unreferenced`）。
黑盒轻行为：构造需真实 `RMIServerImpl` 且会做服务器端初始化，故仅做结构断言（类可加载）。
