# RMIConnector 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnector` 是 RMI 连接器的客户端实现（`implements JMXConnector`，Serializable）。

- 两个 public 构造器：`(RMIServer rmiServer, Map env)`（stub 直连）与 `(JMXServiceURL url, Map env)`；`(null, null)` → IAE `"rmiServer and jmxServiceURL both null"`。
- `serialVersionUID == 817323035842634473`；非 final。
- `connect`/`connect(Map)` 幂等（已连接为 no-op 且保持同一连接 id）；`close` 幂等。
- 消息契约（JDK 源精确串）：未连接 `getConnectionId`/`getMBeanServerConnection` → IOException `"Not connected"`；close 后 connect → IOException `"Connector closed"`；close 后 `getMBeanServerConnection` → IOException `"Connection closed"`；`removeConnectionNotificationListener(null)` → NPE `"listener"`；未注册 listener → `ListenerNotFoundException("Listener not registered")`；死端口 connect → IOException 前缀 `"Failed to retrieve RMIServer stub"`（cause 非空）。
- 发出 OPENED/CLOSING/CLOSED 连接通知（type 字符串、connectionId、source==本 connector）。

## 2. Java 用户如何使用
```java
JMXConnector jmxc = JMXConnectorFactory.connect(new JMXServiceURL(
        "service:jmx:rmi://127.0.0.1"), null);   // 工厂返回 instanceof RMIConnector
MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
jmxc.close();
```

## 3. 开源/源码实际用法调研
- JDK 源 `RMIConnector.java`（jdk8u-dev）：`connect` 双阶段（stub 获取→`newClient`）、消息常量与上述逐一对应。
- `JMXConnectorFactory.connect` 对 rmi 协议返回 `RMIConnector`；stub-encoded URL（`/stub/<base64>`）无需 RMI registry。
来源：JDK 源码 + 双侧 jtreg 实证。

## 4. 测试用例设计
白盒：非 final、双构造器、svuid。
黑盒深化：
- 负例矩阵（§1 全部精确消息）+ close 幂等 + `toJMXConnector` 对客户端无意义（不适用）。
- 回环（工厂 + stub-encoded URL，127.0.0.1 动态端口，finally close/stop）：`jmxc instanceof RMIConnector`、连接 id 前缀 `rmi://127.0.0.1 `、远程 `getAttribute`/`invoke add(5)`/`getMBeanCount==2`、二次 `connect()` no-op 同 id。
- 远程异常消息：`"No such attribute: Nope"`、`"No such operation: nope"`、InstanceNotFoundException 消息即 ObjectName。
- `toJMXConnector`（服务器侧）→ 返回 RMIConnector 且 null 地址。
- 通知：客户端 OPENED/CLOSED（type/id/source）；close 后再断言三状态消息契约。

## 5. 深化（2026-11）与版本差异
- 双侧断言集一致（51/52 级别）；连接 id 无 Subject 时双空格 quirk 双侧一致。
- JDK-27 侧域名区分（jck.rc27）；消息契约双侧逐一相同（该类未受 Subject delegation 移除影响——客户端仅转发）。
