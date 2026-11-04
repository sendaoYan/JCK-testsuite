# RMIServer 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIServer`（接口，继承 `java.rmi.Remote`）是 RMI 连接器服务器的远程接口。

- 方法：`String getVersion() throws RemoteException`、`RMIConnection newClient(Object credentials) throws IOException`。

## 2. Java 用户如何使用
```java
RMIServer stub = ...; // 从 JNDI/RMI 查得
stub.getVersion();                    // 版本串
RMIConnection conn = stub.newClient(credentials);
```
典型组合链路：客户端拿到 `RMIServer` stub 后 `newClient` 建立 `RMIConnection`；`getVersion` 用于协议协商。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIServerImpl.java`：`implements RMIServer`，`getVersion()` 返回 `"1.0 java_runtime_" + java.runtime.version`。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnector.java`：`findRMIServer` 获取并窄化 `RMIServer` stub。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、继承 `Remote`、`getVersion/newClient` 存在。
黑盒轻行为：匿名实现验证 `getVersion/newClient` 可调用。
