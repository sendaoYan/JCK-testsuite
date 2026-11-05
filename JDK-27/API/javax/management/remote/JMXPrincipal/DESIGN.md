# JMXPrincipal 测试设计

## 1. API 功能
`javax.management.remote.JMXPrincipal`（实现 `java.security.Principal`、`Serializable`）表示 JMX Remote API 客户端身份。

- 构造器：`JMXPrincipal(String name)`；null 抛 `NullPointerException`。
- 查询：`getName()`。
- 转换/工具：`toString()` 返回 `JMXPrincipal:  <name>`（冒号后两个空格）；`equals()` 仅对 `JMXPrincipal` 且 `name` 相等（大小写敏感）为真；`hashCode()` 为 `name.hashCode()`。

## 2. Java 用户如何使用
```java
JMXPrincipal p = new JMXPrincipal("user");
p.getName();  // "user"
p.equals(new JMXPrincipal("user")); // true
```
典型组合链路：`JMXAuthenticator.authenticate(credentials)` 返回 `Subject`，其中可包含 `JMXPrincipal`；授权/审计时遍历 `Subject.getPrincipals()` 用 `equals/hashCode` 判定身份。

## 3. 开源/源码实际用法调研
- `test/jdk/javax/management/remote/mandatory/RemovedSubjectDelegation.java`、`test/jdk/javax/management/remote/mandatory/passwordAuthenticator/SimpleStandard.java`、`test/jdk/javax/management/remote/mandatory/connection/ConnectionTest.java`：在构造 `Subject` 时 `add(new JMXPrincipal(...))` 作为认证身份。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXPrincipal.java`：`toString()` 为 `"JMXPrincipal:  " + name`，`validate(null)` 抛 `illegal null input`。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
黑盒部分（`JMXPrincipalTest.testBlackBox()`）：
- 正常路径：`getName`、`toString` 精确值、`equals/hashCode` 一致。
- 组合/边界：空字符串名合法；大小写敏感不等；与 String/null 不等；自反性。
- 异常（精确 message）：null name → `illegal null input`（NPE）。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `JMXPrincipalTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 值语义矩阵 + null 负例 + Subject 组合 + 序列化。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
