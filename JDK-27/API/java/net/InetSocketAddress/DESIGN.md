# java.net.InetSocketAddress 测试设计案（JDK-27）

> 覆盖目标：完整契约。三个构造器 + createUnresolved、null InetAddress 的 wildcard 语义、
> 端口越界→IllegalArgumentException、getPort/getAddress/getHostName/getHostString/isUnresolved/toString、
> resolved/unresolved 的 equals/hashCode、序列化。解析只用 loopback 字面量或显式 hostname（无网络）。

## 1. API 功能

- 构造器：`(int port)`、`(InetAddress,int)`、`(String host,int)`；工厂 `createUnresolved(String,int)`。
- 访问：`getPort/getAddress/getHostName/getHostString/isUnresolved/toString/equals/hashCode`、Serializable。

## 2. 用户如何使用

- `new InetSocketAddress(port)` 绑定 wildcard；`new InetSocketAddress(host,port)` 绑定解析地址；
  `createUnresolved` 生成未解析地址（无反向解析）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/InetSocketAddress.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/InetSocketAddress/`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/InetSocketAddress/`
  （CtorTests、UnresolvedTests、EqualsTests、serial）。

## 4. 测试用例设计（映射到 InetSocketAddressTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 端口构造器合法/非法边界 + wildcard | testPortConstructor |
| P0 | InetAddress 构造器 + null→wildcard + 非法端口 | testInetAddressConstructor |
| P0 | String 构造器（loopback 字面量）+ 非法端口 | testStringConstructor |
| P0 | createUnresolved + null host/非法端口 | testUnresolved |
| P0 | equals/hashCode（unresolved host 大小写不敏感；resolved!=unresolved） | testEqualsHashCode |
| P1 | 序列化 round-trip（resolved + unresolved） | testSerialization |

## 易错规范细节（已锁入断言）

- 合法端口 0..65535；`<0` 或 `>65535` 抛 IllegalArgumentException（0 合法）。
- `new InetSocketAddress(null, port)` 与 `new InetSocketAddress(port)` → wildcard（isAnyLocalAddress）。
- `createUnresolved(null, port)` 抛 IllegalArgumentException。
- unresolved equals 对 host **大小写不敏感**；resolved 与 unresolved（即使同 IP 字符串）**不相等**。
- `getHostString` 返回构造时传入的字符串（InetAddress 构造时返回其 hostname/地址）。

## JDK-27 补充

- unresolved `toString()` 为 `host/<unresolved>:port`（JDK 18+ 增加 `/` + `<unresolved>` 标记；JDK-8 为 `host:port`）。

## 深化补充（批次 E）

### 缺口分析（深化前 65/73 断言：缺 IAE 精确消息、null host 负例、通配语义、IPv6 形式、深度序列化）

### 新增维度（→ 方法）

| 维度 | 方法 | 锁入断言 |
|---|---|---|
| 端口校验精确消息 | testPortValidationExactMessages | 四个构造/工厂 × 6 个非法端口，消息恒 `port out of range:<port>`；0/1/65535 合法 |
| null host 负例 | testNullHostRejected | createUnresolved(null,80) 与 new ISA((String)null,80) 均 IAE `hostname can't be null` |
| 通配语义 | testWildcardSemantics | port 构造器 → any-local 地址；toString `0.0.0.0/0.0.0.0:80` 精确；(InetAddress)null 同义且 equals/hashCode 相等 |
| 未解析深挖 | testUnresolvedDeep | `.invalid` 保留域（永不解析、无 DNS 依赖）；hostString/getHostName 大小写保持；equals 按主机名（忽略大小写）+端口 |
| resolved vs unresolved | testResolvedVsUnresolvedEquality | 同字面量文本的 resolved 与 unresolved 互不 equals（双向）；resolved equals 按 InetAddress 字节+端口 |
| IPv6 形式 | testIPv6Forms | hostString 为**全展开** v6 文本；toString 两侧见「版本差异」 |
| 深度序列化 | testSerializationDeep | 通配/带名 resolved/unresolved round-trip 全字段保持；SocketAddress 超类往返 |

### 易错规范细节（新增实证）

- IAE 消息格式 `port out of range:-1`（无空格，冒号后直接跟数字）。
- `new InetSocketAddress((String) null, 80)` 抛 IAE（与 createUnresolved(null,...) 同消息）——**不是**构造未解析地址。
- getHostString 对字面量构造返回**规范化文本**（`::1` 输入 → `0:0:0:0:0:0:0:1`），不是原始输入。
- 未解析 equals 的主机名比较忽略大小写；getHostName 对未解析地址永不触发 DNS。

### 版本差异（探针实证）

- 未解析 toString：JDK-8 `host:443`；JDK-27 `host/<unresolved>:443`。
- IPv6 toString：JDK-8 `/0:0:0:0:0:0:0:1:80`（无方括号）；JDK-27 `/[0:0:0:0:0:0:0:1]:80`（带方括号）。
- 两侧各自断言精确形式。
