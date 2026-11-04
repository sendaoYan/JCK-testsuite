# java.net.InetAddress 测试设计案（JDK-8）

> 覆盖目标：完整契约。getByAddress（byte[] 与 host+byte[]）、地址长度校验→UnknownHostException、
> 点分十进制格式化、getAddress 防御性拷贝、equals/hashCode 仅比较地址字节、
> 地址分类（any-local/loopback/link-local/site-local/multicast，IPv4+IPv6 字面量）、
> localhost/loopback、序列化。**不触发反向 DNS**（主机名一律显式提供）。

## 1. API 功能

- 工厂：`getByAddress(byte[])`、`getByAddress(String,byte[])`、`getByName`、`getAllByName`、
  `getLoopbackAddress`、`getLocalHost`。
- 访问：`getAddress/getHostAddress/getHostName/getCanonicalHostName`。
- 分类：`isAnyLocalAddress/isLoopbackAddress/isLinkLocalAddress/isSiteLocalAddress/isMulticastAddress` + MC* 范围方法。
- `equals/hashCode/toString`、Serializable。

## 2. 用户如何使用

- `getByName("localhost")`/`getLoopbackAddress()` 拿本机回环；`getByAddress(byte[])` 从字面字节构造。
- 用 `is*` 系列做地址分类；`getAddress()` 拿原始字节。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/InetAddress.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/InetAddress/`（IPv4Formats、BadDottedIPAddress、GetLoopbackAddress、HashSpread）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/InetAddressTest.java` + InetAddress/。

## 4. 测试用例设计（映射到 InetAddressTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | getByAddress 构造 + 点分十进制 + 非法长度→UnknownHostException | testGetByAddress |
| P0 | getAddress 返回防御性拷贝 | testDefensiveCopy |
| P0 | equals/hashCode 仅比较地址字节（忽略 hostname） | testEqualsHashCode |
| P0 | 地址分类（IPv4 any/loop/link/site/multicast + IPv6 ::/::1/fe80/ff00） | testClassification |
| P0 | localhost/loopback 结构合法（长度 4 或 16、isLoopbackAddress） | testLocalhostAndLoopback |
| P1 | 序列化 round-trip | testSerialization |

## 易错规范细节（已锁入断言）

- `getByAddress(byte[])` 长度必须 4 或 16，否则 UnknownHostException；`getAddress()` 返回克隆，改动不影响原对象。
- `equals/hashCode` 只比较地址字节，**不**比较 hostname（同字节不同 hostname 相等）。
- 反向 DNS 陷阱：`getByAddress(byte[]).getHostName()` 会触发反向解析；测试一律用 `getByAddress(String,byte[])` 显式 hostname。
- 分类边界：127.0.0.0/8 loopback；169.254/16 link-local；10/8、172.16/12、192.168/16 site-local；224-239/4 multicast。

## 深化补充（批次 E，41→343 断言；全部行为先经双 JDK 探针实证）

### 缺口分析（深化前只有 41 断言的浅冒烟，缺以下全部维度）

字面量矩阵、IPv6 归一化、跨构造 equals、hashCode 精确值、MC 范围方法、isReachable 状态机、
toString 形式、序列化类型保持等均未覆盖。

### 新增维度（→ 方法）

| 维度 | 方法 | 锁入断言 |
|---|---|---|
| IPv4 字面量矩阵 + 遗留形式 + 非法矩阵 | testIPv4LiteralMatrix | getByName→Inet4Address、往返；`1.2.3`→1.2.0.3、`01.2.3.4`→1.2.3.4（遗留接受）；8 种非法字面量→UHE |
| IPv6 字面量矩阵 + 归一化 | testIPv6LiteralMatrix | **getHostAddress 恒为全展开形式**（`::1`→`0:0:0:0:0:0:0:1`）；`::FFFF:127.0.0.1`→**Inet4Address**（IPv4-mapped 折叠）；`[::1]` 接受；16 字节往返 |
| null/localhost/localHost/loopback | testNullHostAndLocalhost | getByName(null)→回环；getLoopbackAddress→Inet4Address `127.0.0.1`（双栈实测）；getLocalHost 仅断非 null |
| getByAddress 完整矩阵 | testGetByAddressMatrix | 10 组字节往返+equals+hashCode；**getByAddress(null)→UHE（非 NPE）**，消息含 "illegal length"；byte[5]/byte[3]/byte[0]→UHE |
| hashCode 精确值 | testHashCodeExactValues | 127.0.0.1→2130706433、1.2.3.4→16909060、`::`→0、`::1`→1（地址即哈希） |
| IPv4 分类边界 | testClassificationIPv4Boundaries | 127/8 全段、126/128 界外；169.254/16 两端；10/8、172.16/12（172.15/172.32 界外）、192.168/16；224-239/4 两端（223/240 界外） |
| IPv6 分类边界 | testClassificationIPv6Boundaries | fe80::/10（fe7f 界外、febf 界内）；**fec0::/10 site-local（fc00/fd00 ULA 不是）**；ff00::/8 组播两端 |
| MC 范围矩阵 | testMulticastScopeMatrix | 224.0.0.x link / 224.0.1.x global / 239.192.x org / 239.255.x site / 239.251 none；ff01 node / ff02 link / ff05 site / ff08 org / ff0e global / ff0f none；IPv4 无 node-local |
| isReachable 状态机 | testReachability | 127.0.0.1/::1 reachable(200)、reachable(0)=true；负 timeout→IAE **"timeout can't be negative"**；netif 变体 null 接口合法、负 timeout IAE |
| 跨构造 equals | testCrossConstructionEquality | getByName/getByAddress/named getByAddress 三路同字节互相 equals+同 hashCode；v4≠v6 对称 |
| 序列化类型保持 | testSerializationDeep | Inet4/Inet6 具体类型、scope、hostname、分类标志全部跨序列化保持 |

### 易错规范细节（新增实证）

- `getByName("::1")` 的 `getHostAddress()` 是 `0:0:0:0:0:0:0:1`，**不是**压缩形式 `::1`。
- `getByName("::ffff:127.0.0.1")` 返回 Inet4Address（IPv4-mapped 折叠为 4 字节），且与字面量 `127.0.0.1` equals。
- `getByAddress(null)` 抛 UnknownHostException（"addr is of illegal length"），**不是** NPE。
- `isSiteLocalAddress` 对 fec0::/10 为 true；fc00::/7（ULA）为 false。
- Inet6Address.hashCode = 前 4 个 int 的 XOR（`::`→0、`::1`→1）。
- getByName("localhost") 的 toString 保留 `localhost/...` 前缀；字面量查询的 toString 是 `/dotted-quad`。
