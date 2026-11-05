# JMXConnectorServerFactory：服务器工厂（newJMXConnectorServer + 环境键常量） 测试设计（JDK-8 侧）

> 2026-11 批 m 契约深化：原有白盒反射冒烟之上追加 `testDeepened()`（黑盒契约 + RMI 真回环）。

## 1. API 功能
- env null/空 map/未知键 三形态产物等价（均 wire MBeanServer、实现 JMXConnectorServerMBean 与 JMXAddressable）；null MBeanServer 允许创建（start 被拒于基类）。
- 常量：PROTOCOL_PROVIDER_PACKAGES/DEFAULT_CLASS_LOADER/DEFAULT_CLASS_LOADER_NAME/PROTOCOL_PROVIDER_CLASS_LOADER 四值。
- 负例：未知协议 → `MalformedURLException: Unsupported protocol: bogus`；坏 provider 包 + rmi 仍成功（ServiceLoader 兜底，锁定行为）。

## 2. 测试用例设计与易错规范细节
- 常量、env 矩阵、协议负例、一次完整回环（start/connect/read/close/stop）。

## 3. 参考来源
- JDK8u-dev `JMXConnectorServerFactory.java`；双侧运行实测。


### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
