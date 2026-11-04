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
