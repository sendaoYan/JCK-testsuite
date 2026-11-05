# RMIServerImpl 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIServerImpl`（abstract，implements RMIServer）是 RMI 连接器服务器的可继承基类。

- public `(Map env)` 构造器（null 容忍）；protected 钩子：`toStub/export/makeClient/closeClient/getProtocol/closeServer/clientClosed/connectionOpened/connectionClosed`；`setMBeanServer/getMBeanServer`、`setDefaultClassLoader/getDefaultClassLoader`（均容忍 null）。
- `getVersion()` = `"1.0 java_runtime_" + 版本`（public final 行为）。
- `newClient` 无 MBeanServer → ISE `"Not attached to an MBean server"`；挂了 MBeanServer 但未挂 connector server（裸 impl）→ NPE（无自定义消息，来自 connServer 空引用）。
- `close()` 幂等（先关全部客户端再 `closeServer`）。
- 非 Serializable。

## 2. Java 用户如何使用
```java
RMIServerImpl impl = new RMIJRMPServerImpl(0, null, null, null); // 工厂也走这条链
impl.setMBeanServer(mbs);
RMIConnectorServer cs = new RMIConnectorServer(url, env, impl, mbs);
```
测试用最小子类 `TinyServer`（toStub 返回 null、全部钩子 no-op、`getProtocol()="tiny"`、覆写 `clientClosed` 跳过 connServer 记账）直接驱动基类行为。

## 3. 开源/源码实际用法调研
- JDK 源 `RMIServerImpl.java`（jdk8u-dev）：`doNewClient`/`makeConnectionId`（`"rmi://<clientHost> <principals> <counter>"`）、连接记账 `connectionOpened/Closed` 通知宿主 connector server。
- `clientClosed` 默认实现回调 connServer——裸 impl 下会 NPE，故子类覆写。
来源：JDK 源码 + 双侧 jtreg 实证。

## 4. 测试用例设计
白盒：abstract、protected 钩子全集、public `(Map)` ctor、非 Serializable。
黑盒深化：
- 结构：`getVersion` 前缀、`setMBeanServer(null)`/`setDefaultClassLoader(null)` 容忍（getMBeanServer 回 null）。
- 行为：`newClient` 无 MBS → ISE 精确消息；`tinyWith(mbs)` 裸 impl（覆写 clientClosed）`newClient` → NPE；`close()` 连续两次幂等（closeServer no-op）。
- 连接 id 语义经 makeConnectionId 路径（见 RMIServer/连接器测试）覆盖。

## 5. 深化（2026-11）与版本差异
- 裸 impl newClient 的 NPE 差异：无 MBS 先决检查 ISE 优先；有 MBS 但无 connServer 时 NPE（顺序敏感，断言分别覆盖）。
- 双侧行为一致（JDK-27 无差异——基类未受 IIOP 移除影响）；JDK-27 侧域名区分。
