# java.net.spi.InetAddressResolver 设计文档

## 1. API 功能

主机名解析 SPI 接口，及嵌套 `LookupPolicy`（解析策略）。

- 方法：`lookupByName(String host, LookupPolicy policy)`（返回 `Stream<InetAddress>`）、
  `lookupByAddress(byte[] addr)`（反向解析为名字）。
- `LookupPolicy`：常量 `IPV4`/`IPV6`/`IPV4_FIRST`/`IPV6_FIRST`；工厂 `of(int characteristics)`；查询 `characteristics()`。

## 2. Java 用户如何使用

- 实现类：实现 `lookupByName`/`lookupByAddress`，经 `InetAddressResolverProvider` 提供。
- 策略：`LookupPolicy.of(IPV4 | IPV4_FIRST)` 表达「只查 IPv4 且优先」；
  非法掩码（无 IPV4/IPV6、或同时两个 FIRST）抛 `IllegalArgumentException`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/net/spi/InetAddressResolver.java`）：
  `LookupPolicy` 使用位掩码（IPV4=1, IPV6=2, IPV4_FIRST=4, IPV6_FIRST=8），`of` 校验掩码合法性。

## 4. 测试用例设计 + 自动化实现

文件：`InetAddressResolverTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public interface / 无父类 / `lookupByName`/`lookupByAddress` 方法面。
- 黑盒：匿名实现 → `lookupByName` 返回解析流、`lookupByAddress` 返回名字 →
  `LookupPolicy.of` 各种合法掩码与 `characteristics()` 读回 → 非法掩码异常。
