# RMIJRMPServerImpl 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIJRMPServerImpl`（继承 `RMIServerImpl`）是 JRMP 传输的服务器端实现。

- 构造器：`(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, Map env)`；`port < 0` 抛 `IllegalArgumentException`。
- 方法：`toStub()`、受保护的 `export/getProtocol/makeClient/closeClient/closeServer`，继承的 `getVersion/setMBeanServer/getMBeanServer/setDefaultClassLoader/getDefaultClassLoader`。

## 2. Java 用户如何使用
该实现通常由 `RMIConnectorServer.newServer()` 创建；端口 0 表示随机端口。用户不直接构造，或仅用 `getVersion/setMBeanServer` 等继承方法做配置。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnectorServer.java`：`newJRMPServer` 创建 `RMIJRMPServerImpl`。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIJRMPServerImpl.java`：构造校验 `Negative port`，`getProtocol()` 返回 `"rmi"`。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：具体类、继承 `RMIServerImpl`、`toStub/export/getProtocol` 存在。
黑盒轻行为：`new RMIJRMPServerImpl(0, null, null, null)` 不绑定端口；`getVersion` 前缀 `"1.0 "`、`set/getMBeanServer`、`set/getDefaultClassLoader` 往返；负端口抛 `IllegalArgumentException("Negative port: -1")`。不调用 `close()`（未导出的 JRMP 服务器 `closeServer()` 会 `unexportObject` 抛 `NoSuchObjectException`）。

## 5. 深化（2026-11）
白盒新增：protected 钩子全集、非 final、非 Serializable。
黑盒新增：
- 未导出：`toStub()` → `NoSuchObjectException("object not exported")`；`newClient` 无 MBS → ISE `"Not attached to an MBean server"`；`close()` 未导出 → `NoSuchObjectException("object not exported")`（此前设计回避 close，现以精确消息锁定）。
- 一体化 live：4 参 ctor 挂 `RMIConnectorServer` → start → `toStub() instanceof RMIServerImpl_Stub`、`stub.getVersion()==impl.getVersion()`、`stub.newClient(...)` → `RMIConnectionImpl_Stub`、协议级 `getAttribute/invoke add/queryNames/isRegistered/getDefaultDomain/getDomains/getMBeanInfo/isInstanceOf`、连接 id 记账与 `rc.close()` 移除；`cs.stop()` 后死 stub `newClient` → IOException（stub 引用在 stop 前捕获——stop 后 `impl.toStub()` 本身抛 NoSuchObjectException）。
版本差异：消息与行为与 JDK-8 逐串一致；port=0 动态端口仅回环双侧可用。
