# MBeanServerForwarder 测试设计

## 1. API 功能
`javax.management.remote.MBeanServerForwarder`（接口，继承 `MBeanServer`）在 `MBeanServer` 请求链中作中间转发器，可拦截/改写请求。

- 方法：`MBeanServer getMBeanServer()`、`void setMBeanServer(MBeanServer)`（外加继承的全部 `MBeanServer` 方法）。

## 2. Java 用户如何使用
```java
MBeanServerForwarder fwd = ...; // 自定义转发器
fwd.setMBeanServer(delegate);
MBeanServer next = fwd.getMBeanServer();
```
典型组合链路：`JMXConnectorServer.setMBeanServerForwarder(fwd)` 把转发器插入请求链；转发器把请求转给 `getMBeanServer()`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServer.java`：`setMBeanServerForwarder` 保存转发器并把 `MBeanServer` 指向其 `getMBeanServer()`。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/MBeanServerForwarder.java`：接口定义。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、继承 `MBeanServer`、`getMBeanServer/setMBeanServer` 存在。
黑盒轻行为：用 `java.lang.reflect.Proxy` 动态实现接口（避免手写约 30 个 `MBeanServer` 方法），验证 `setMBeanServer → getMBeanServer` 往返。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `MBeanServerForwarderTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 契约矩阵（动态代理记录型）+ RMI 链路插桩（调用名录制 + 透传保真）。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
