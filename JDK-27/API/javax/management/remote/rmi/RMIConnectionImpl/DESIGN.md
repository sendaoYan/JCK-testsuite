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

## 5. 深化（2026-11）
白盒新增：非 final、唯一 5 参 public 构造器、非 Serializable。
黑盒新增（TinyServer 直构造实现本地路由，覆写 clientClosed 跳过 connServer 记账）：null server / null connectionId → NPE `"Illegal null argument"`（null env 容忍）；`getConnectionId` 往返、`toString` 含 `connectionId=`；`getAttribute/invoke/setAttribute+回读/getAttributes/isRegistered/getMBeanCount/getDefaultDomain/getDomains/getMBeanInfo/isInstanceOf/getObjectInstance/createMBean/queryNames/unregisterMBean` 全链路本地路由真值；`invoke` null name → `RuntimeOperationsException` 包 IAE `"ObjectName must not be null"`、null op → `"Operation name must not be null"`；`close()` 幂等且 close 后本地调用仍可用（quirk）；`unreferenced()` 连续两次无害。
**版本差异（关键）**：delegation subject（env 含 `jmx.remote.subject.delegate` 凭据映射）在 JDK-8 抛 `SecurityException("Subject delegation cannot be enabled unless an authenticated subject is put in place")`，本版本抛 `UnsupportedOperationException("Subject Delegation has been removed.")`——Subject delegation 特性整体移除。
坑：宿主 impl 必须先 setMBeanServer，否则构造器在 `getClassLoaderRepository()` 处无消息 NPE。
