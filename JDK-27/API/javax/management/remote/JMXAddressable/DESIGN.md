# JMXAddressable 测试设计

## 1. API 功能
`javax.management.remote.JMXAddressable`（接口）提供连接器/连接器服务器的地址。

- 方法：`JMXServiceURL getAddress()` —— 返回地址，可为 null（如仅持有 RMI stub 的连接器）。

## 2. Java 用户如何使用
```java
JMXAddressable a = new JMXConnectorServer(...) /* 或 JMXConnector 实现 */;
a.getAddress(); // 服务/连接地址
```
典型组合链路：`RMIConnector`、`JMXConnectorServer` 均实现该接口；用户对任一连接端点统一调用 `getAddress()`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnector.java`（`getAddress()` 返回 `jmxServiceURL`）与 `RMIConnectorServer.java`（未启动时返回 null）：JDK 内部实现点。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServer.java`：声明实现 `JMXAddressable`。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、`getAddress()` 返回类型为 `JMXServiceURL`。
黑盒轻行为：匿名实现证明 `getAddress()` 可调用，且能返回 null 或指定值。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `JMXAddressableTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 服务器三阶段地址 + 客户端地址 + 用户实现 round-trip。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
