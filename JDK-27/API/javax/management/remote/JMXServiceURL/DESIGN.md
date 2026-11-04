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
