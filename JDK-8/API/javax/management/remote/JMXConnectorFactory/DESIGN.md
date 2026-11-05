# JMXConnectorFactory：客户端工厂（connect/newJMXConnector + 环境键常量） 测试设计（JDK-8 侧）

> 2026-11 批 m 契约深化：原有白盒反射冒烟之上追加 `testDeepened()`（黑盒契约 + RMI 真回环）。

## 1. API 功能
- connect(url) ≡ connect(url, null)；newJMXConnector 只建连不连接（getConnectionId → IOException "Not connected"）。
- 常量：PROTOCOL_PROVIDER_PACKAGES=`jmx.remote.protocol.provider.pkgs`、DEFAULT_CLASS_LOADER=`jmx.remote.default.class.loader`、PROTOCOL_PROVIDER_CLASS_LOADER=`jmx.remote.protocol.provider.class.loader`。
- 负例：connect(null) → `NullPointerException: Null JMXServiceURL`；未知协议 → `MalformedURLException: Unsupported protocol: bogus`；坏 provider 包 + rmi → rmi provider 经 ServiceLoader 仍命中，失败点变为 `MalformedURLException: URL path must begin with /jndi/ or /stub/ or /ior/`；死端口 → IOException（cause 链底为 java.net.ConnectException）。

## 2. 测试用例设计与易错规范细节
- 常量、双 connect 形态回环、newJMXConnector 未连接态、四类负例。

## 3. 参考来源
- JDK8u-dev `JMXConnectorFactory.java` 与 `ClientProvider` ServiceLoader 注册机制；双侧运行实测。


### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
