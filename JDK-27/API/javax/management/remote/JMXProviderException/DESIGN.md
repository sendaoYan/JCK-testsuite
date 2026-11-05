# JMXProviderException 测试设计

## 1. API 功能
`javax.management.remote.JMXProviderException`（继承 `java.io.IOException`）表示找不到或无法实例化 JMX 连接器提供者。

- 构造器：`()`、`(String message)`、`(String message, Throwable cause)`。
- 查询：`getCause()` 返回构造时传入的 `Throwable`（覆盖 `Throwable.getCause()`），其余构造下为 null。

## 2. Java 用户如何使用
```java
throw new JMXProviderException("no provider", new ClassNotFoundException("x"));
// 调用方 catch (JMXProviderException e) 后读 e.getMessage() / e.getCause()
```
典型组合链路：`JMXConnectorFactory`/`JMXConnectorServerFactory` 在 `resolvePkgs`/加载提供者失败时抛 `JMXProviderException`，上层按 `IOException` 或专用类型捕获并诊断 `getCause()`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorFactory.java`：`resolvePkgs` 与 `getProvider` 在提供者解析失败时 `throw new JMXProviderException(...)`，是 JDK 内部直接使用点。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXProviderException.java`：`getCause()` 返回私有 `cause` 字段。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
黑盒部分（`JMXProviderExceptionTest.testBlackBox()`）：
- 正常路径：`(message, cause)` 保留 message 与同一 cause 实例、`instanceof IOException`。
- 组合/边界：仅 message 构造 cause 为 null；无参构造 message/cause 均 null；`addSuppressed` 等标准 `Throwable` 机制可用。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `JMXProviderExceptionTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 构造矩阵 + 层级 + 序列化。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
