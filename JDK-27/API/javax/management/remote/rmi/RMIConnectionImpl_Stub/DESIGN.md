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

## 5. 深化（2026-11）
白盒新增：`serialVersionUID == 2`、Serializable、无 `getVersion`（连接级 stub 与服务器级区分）。
黑盒新增：
- RecordingRef（自定义 `RemoteRef` 记录 method/args）：验证 `getConnectionId/getAttribute(三参)/close/fetchNotifications/isRegistered/getMBeanCount` 全部经 `ref.invoke` 委托（生成 stub 100% 走反射式 RemoteRef 协议）；`isRegistered` 前置 reply=Boolean.FALSE、`getMBeanCount` 前置 Integer.valueOf(0)（防拆箱 NPE）；`fetchNotifications` 前置 reply=null（stub 对返回值生成期 cast）；`new RMIConnectionImpl_Stub(null)` 调用 → NPE。
- 真线 stub：`RMIJRMPServerImpl`+`RMIConnectorServer` 回环，`newClient(null)` 返回 `instanceof RMIConnectionImpl_Stub`，线级 `getAttribute`/`invoke add(6)` 真值、连接 id 前缀；`rc.close()` 后死 stub 抛 IOException。
版本差异：本类两版本均存在（客户端 JRMP stub 未移除）；svuid=2 双侧一致；仅 IIOP 生成物（`_RMIConnection_Stub`/Tie）被移除。
