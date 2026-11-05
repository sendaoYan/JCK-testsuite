# JMXServiceURL 测试设计

## 1. API 功能
`javax.management.remote.JMXServiceURL`（实现 `Serializable`）表示 JMX Remote API 服务地址，形如 `service:jmx:<protocol>://<host>[:<port>]/<path>`。

- 构造器：
  - `JMXServiceURL(String serviceURL)` —— 解析完整 URL 字符串。
  - `JMXServiceURL(String protocol, String host, int port)` —— 等价于四参构造传 null path。
  - `JMXServiceURL(String protocol, String host, int port, String urlPath)` —— 逐段构造；`urlPath` 为 null 时默认为空串；`port` 不能为负。
- 查询：`getProtocol()`（小写）、`getHost()`（可为空串，IPv6 去方括号）、`getPort()`（无端口时为 0）、`getURLPath()`（空串或以 `/`、`;` 开头）。
- 转换/工具：`toString()`（可回馈构造器，端口 0 省略）、`equals()`（protocol/host 忽略大小写，port/path 严格）、`hashCode()`（`toString().hashCode()`，因此对 host 大小写敏感）。

## 2. Java 用户如何使用
```java
JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
url.getProtocol();   // "rmi"
url.getHost();       // ""
url.getPort();       // 0
url.getURLPath();    // "/jndi/rmi://localhost:9999/server"
```
典型组合链路：`new JMXServiceURL(str) → getProtocol/getHost/getPort/getURLPath → toString 回馈 → equals/hashCode 比较`；再用 `JMXConnectorFactory.connect(url, env)` 建立连接。

## 3. 开源/源码实际用法调研
- `test/jdk/com/sun/tools/attach/StartManagementAgent.java`（JDK 自带测试，166–223 行）：先用 `new JMXServiceURL(result)` 校验返回值可解析，再 `new JMXServiceURL(jmxUrlStr)`，最后 `JMXConnectorFactory.connect(url, env)`。
- `test/jdk/com/sun/jmx/remote/NotificationMarshalVersions/Server/Server.java`：构造 `JMXServiceURL` 作为连接地址。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXServiceURL.java`：自身 `toString()` 用 `getProtocol()/getHost()/getPort()/getURLPath()` 拼接，`equals()` 对 protocol/host 用 `equalsIgnoreCase`。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`，非记忆编造。

## 4. 测试用例设计
黑盒部分（`JMXServiceURLTest.testBlackBox()`）：
- 正常路径：解析 JRMP 空 host + JNDI path URL，断言四段取值与 `toString` 回环；解析 jmxmp `host:1234` URL。
- 组合/边界：协议小写化、零端口在 `toString` 中省略、`equals` 大小写不敏感 + 不同 port/path 不等 + 与 String 不等、`hashCode` 相同输入稳定、四参构造 null path 默认空串。
- 异常（精确 message 实测）：`MalformedURLException` 的 8 种稳定 message（缺前缀、缺 `://`、无 host 给端口、负端口、坏 path、非法协议、坏 host、尾随非 path 字符）。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `JMXServiceURLTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 负例（双侧精确消息一致）：缺前缀 `Service URL must start with service:jmx:`；缺 `://` → `Missing "://" after protocol name`；有端口无主机 → `Cannot give port number without host name`；负端口 → `Bad port: -1`；坏路径 → `Bad URL path: <x>`；非法协议 → `Missing or invalid protocol name: "9bad"`；坏主机 → `Bad host: "bad_host"`；尾随垃圾 → `Bad URL path: abc`；null 字符串 → NPE（两侧消息不同，只断言类型）。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
