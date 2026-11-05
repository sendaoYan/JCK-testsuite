# JMXServiceURL：不可变服务地址值对象 测试设计（JDK-8 侧）

> 2026-11 批 m 契约深化：原有白盒反射冒烟之上追加 `testDeepened()`（黑盒契约 + RMI 真回环）。

## 1. API 功能
- 构造：`JMXServiceURL(String serviceURL)` 解析 `service:jmx:<proto>://[host[:port]][/urlPath]`；`(protocol,host,port)` 等价 `(protocol,host,port,null)`；port 仅拒绝负数（0/65535/65536 均接受，>65535 为锁定 quirk）；null urlPath 归一空串。
- 读取：getProtocol（小写归一）、getHost（保留原大小写；IPv6 去方括号）、getPort（无端口=0 而非 -1）、getURLPath（JNDI 地址整体留在 path 内）。
- equals/hashCode：protocol/host 忽略大小写；hashCode = toString().hashCode() 保留原大小写 → 大小写变体相等但可不同哈希（锁定 quirk）。
- 序列化 round-trip 相等。

## 2. 测试用例设计与易错规范细节
- 负例（双侧精确消息一致）：缺前缀 `Service URL must start with service:jmx:`；缺 `://` → `Missing "://" after protocol name`；有端口无主机 → `Cannot give port number without host name`；负端口 → `Bad port: -1`；坏路径 → `Bad URL path: <x>`；非法协议 → `Missing or invalid protocol name: "9bad"`；坏主机 → `Bad host: "bad_host"`；尾随垃圾 → `Bad URL path: abc`；null 字符串 → NPE（两侧消息不同，只断言类型）。

## 3. 参考来源
- JDK8u-dev `jdk/src/share/classes/javax/management/remote/JMXServiceURL.java`（大小写/端口/路径解析与 equals/hashCode 实现依据）；Oracle javadoc javax.management.remote 包的 Service URL 语法；双侧 javap 与运行实测。


### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
