# SubjectDelegationPermission：主题委托权限（BasicPermission 子类，无 actions） 测试设计（JDK-8 侧）

> 2026-11 批 m 契约深化：原有白盒反射冒烟之上追加 `testDeepened()`（黑盒契约 + RMI 真回环）。

## 1. API 功能
- 名矩阵：`javax.management.remote.*` 蕴含同名前缀具体目标、反向不成立、reflexive 蕴含；目标名不做命名空间校验（任意串可构造）；implies(null)=false（BasicPermission 行为，不抛）；getActions 恒空串；带 actions 构造 → `IllegalArgumentException: Non-null actions`；null 名 → `NullPointerException: name can't be null`；序列化 round-trip 相等。
- JDK-27 中标记 deprecated for removal（编译警告），语义不变；JDK-8 下未授权委托的真实触发见 JMXConnector。

## 2. 测试用例设计与易错规范细节
- 名/蕴含矩阵 + actions 负例 + 层级 + 序列化。

## 3. 参考来源
- JDK8u-dev `SubjectDelegationPermission.java`、`BasicPermission`；双侧运行实测。


### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
