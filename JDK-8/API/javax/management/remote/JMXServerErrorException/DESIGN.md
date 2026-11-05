# JMXServerErrorException：服务器端 Error 中继（IOException 子类） 测试设计（JDK-8 侧）

> 2026-11 批 m 契约深化：原有白盒反射冒烟之上追加 `testDeepened()`（黑盒契约 + RMI 真回环）。

## 1. API 功能
- 构造 (String,Error)：getCause 同一 Error、null Error 允许（cause 空）；序列化保留 cause。
- 真闭环触发：MBean 方法内抛 Error 会被 MBeanServer 包成 RuntimeErrorException（NOT 本类）；Error 必须从 MBeanServerForwarder 逃逸 —— 经 setMBeanServerForwarder 注入的 forwarder 在 invoke 抛 AssertionError，客户端收 JMXServerErrorException 且 cause 类型/消息保留，非 invoke 路径不受影响。

## 2. 测试用例设计与易错规范细节
- 构造/序列化 + forwarder 逃逸真闭环（cause 保真）+ 对照：MBean 内 Error → RuntimeErrorException。

## 3. 参考来源
- JDK8u-dev `JMXServerErrorException.java` 与 `RMIConnectionImpl`/server dispatcher 的 Error 中继路径；双侧运行实测。


### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
