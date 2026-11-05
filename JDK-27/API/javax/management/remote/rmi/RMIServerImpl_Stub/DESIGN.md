# RMIServerImpl_Stub 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIServerImpl_Stub`（`final`，继承 `java.rmi.server.RemoteStub`，实现 `RMIServer`）是 `RMIServerImpl` 的动态生成 RMI stub。

- 构造器：`(java.rmi.server.RemoteRef ref)`。
- 方法：`getVersion() throws RemoteException`、`newClient(Object) throws IOException`。

## 2. Java 用户如何使用
该 stub 由 RMI 运行时在客户端自动使用；用户通常持有 `RMIServer` 接口引用，不显式实例化 stub。

## 3. 开源/源码实际用法调研
- JDK 生成产物 `javax/management/remote/rmi/RMIServerImpl_Stub.class`：`implements RMIServer`。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnector.java`：`findRMIServer` 窄化到 `RMIServer` stub。
来源均为 JDK 仓库（jdk25u-dev）源码与类文件。

## 4. 测试用例设计
白盒：`final` 类、继承 `RemoteStub`、实现 `RMIServer`、`(RemoteRef)` 构造器存在、`getVersion/newClient` 存在。
黑盒轻行为：stub 需要活跃 `RemoteRef`，故仅做结构断言（类可加载）。类标注 `@SuppressWarnings("deprecation")`（`RemoteStub`/`RemoteRef` 已弃用）。

## 5. 深化（2026-11）
白盒新增：`serialVersionUID == 2`、Serializable、无 `getAttribute`（服务器级 stub 与连接级 stub 区分）。
黑盒新增：
- RecordingRef（自定义 `RemoteRef` 记录 method/args）：`getVersion` 返回 reply 且 ref 观察 `getVersion`；`newClient("creds")` 观察凭据参数透传；`new RMIServerImpl_Stub(null).getVersion()` → NPE。
- 真线 stub：`RMIJRMPServerImpl`+`RMIConnectorServer` 回环，`impl.toStub() instanceof RMIServerImpl_Stub`、真 `getVersion` `"1.0 "` 前缀、`newClient` → `RMIConnectionImpl_Stub`、线级 `getAttribute` 真值；`cs.stop()` 后死 stub `newClient` → IOException。
版本差异：本类两版本均存在（客户端 JRMP stub 未移除）；svuid=2 双侧一致。
