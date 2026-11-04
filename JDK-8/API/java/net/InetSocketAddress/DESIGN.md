# java.net.InetSocketAddress 测试设计案（JDK-8）

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
