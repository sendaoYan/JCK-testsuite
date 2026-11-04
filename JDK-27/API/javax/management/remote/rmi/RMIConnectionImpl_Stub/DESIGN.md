# RMIConnectionImpl_Stub 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnectionImpl_Stub`（`final`，继承 `java.rmi.server.RemoteStub`，实现 `RMIConnection`）是 `RMIConnectionImpl` 的动态生成 RMI stub。

- 构造器：`(java.rmi.server.RemoteRef ref)`。
- 方法：实现 `RMIConnection` 的全部远程方法（含 `getConnectionId/close/createMBean/getAttribute/fetchNotifications`）。

## 2. Java 用户如何使用
该 stub 由 RMI 运行时在客户端自动使用；用户通常直接持有 `RMIConnection` 接口引用，不显式实例化 stub。

## 3. 开源/源码实际用法调研
- JDK 生成产物 `javax/management/remote/rmi/RMIConnectionImpl_Stub.class`：`implements RMIConnection`。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnector.java`：客户端窄化 `RMIConnection` 到 stub。
来源均为 JDK 仓库（jdk25u-dev）源码与类文件。

## 4. 测试用例设计
白盒：`final` 类、继承 `RemoteStub`、实现 `RMIConnection`、`(RemoteRef)` 构造器存在、关键方法存在。
黑盒轻行为：stub 需要活跃 `RemoteRef` 才能做 RMI 调用，故仅做结构断言（类可加载）。类标注 `@SuppressWarnings("deprecation")`（`RemoteStub`/`RemoteRef` 已弃用）。
