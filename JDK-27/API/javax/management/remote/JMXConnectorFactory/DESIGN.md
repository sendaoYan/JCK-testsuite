# JMXConnectorFactory 测试设计

## 1. API 功能
`javax.management.remote.JMXConnectorFactory`（工具类，私有构造）创建 JMX 连接器客户端。

- 常量：`PROTOCOL_PROVIDER_PACKAGES`、`DEFAULT_CLASS_LOADER`、`PROTOCOL_PROVIDER_CLASS_LOADER`。
- 静态方法：`connect(JMXServiceURL)`、`connect(JMXServiceURL, Map)`、`newJMXConnector(JMXServiceURL, Map)`（仅创建实例，不建立连接）。

## 2. Java 用户如何使用
```java
JMXConnector c = JMXConnectorFactory.newJMXConnector(url, null); // 不连接
// 或
JMXConnector c2 = JMXConnectorFactory.connect(url, env);         // 创建并连接
```
典型组合链路：`newJMXConnector` 用协议名查提供者包，实例化 `RMIConnector`；`connect` 再调用 `connect(env)`。

## 3. 开源/源码实际用法调研
- `test/jdk/com/sun/tools/attach/StartManagementAgent.java`（217–223 行）：`JMXConnectorFactory.connect(url, env)`。
- `test/jdk/com/sun/jmx/remote/NotificationMarshalVersions/Client/Client.java`：`JMXConnectorFactory.connect`。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorFactory.java`：协议名解析 + 服务加载逻辑。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
白盒：无公共构造器、`connect/newJMXConnector` 方法存在。
黑盒轻行为：三个常量精确值；`newJMXConnector(rmi url, null)` 返回 `RMIConnector`（未连接）；未连接时 `getConnectionId()` 抛 `IOException("Not connected")`；未知协议抛 `MalformedURLException("Unsupported protocol: ...")`。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `JMXConnectorFactoryTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 常量、双 connect 形态回环、newJMXConnector 未连接态、四类负例。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
