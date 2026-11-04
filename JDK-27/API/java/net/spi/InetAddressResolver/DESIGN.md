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

## 5. 深化缺口分析(批次 J,2026-11-05)

原 14 断言只有形状 + 一次黑盒冒烟。深化到 80 断言(6 个新部分):

- **方法形状精确化**(probed):恰好 2 个声明方法、均 abstract + 唯一 throws
  `UnknownHostException`、`lookupByName` 泛型返回 `Stream<InetAddress>`、
  无字段/无 default/无 static、非 @FunctionalInterface、非 sealed。
- **参数透传**:RecordingResolver 记录 host/LookupPolicy/byte[] 调用序列;
  LookupPolicy **引用级**透传(`==`)、null host/null policy 由实现自便、
  byte[] 引用与 16 字节 IPv6 内容透传。
- **Stream 语义**:空流/单元素/双元素保序;返回流单次消费(二次 terminal →
  IllegalStateException);`onClose` 经 try-with-resources 触发;filter/allMatch
  管道可用。
- **异常中继**:UHE/RuntimeException/UHE(byte[]) 均**同一异常对象**透传;
  `lookupByAddress` 返回 null 不被包装。
- **动态代理实现**:接口可被 `Proxy.newProxyInstance` 实现(与 sealed 的
  Configuration 对比),handler 收到 byte[] 原引用。
- **真实语义 + 隔离**:literal resolver(`getByAddress(host, 127.0.0.1 字节)`)
  hostname/address round-trip、`isLoopbackAddress`;5 字节 → UHE(经
  InetAddress 契约);**未注册 provider 时内置解析路径不受影响**
  (`getAllByName("127.0.0.1")`/`getByName(null)` 仍工作)。

LookupPolicy 专项见 `LookupPolicy/DESIGN.md`(1 → 114 断言:0..15 表驱动、
三种精确 IAE 消息、保留位宽松行为、identity 语义)。

## 6. 版本差异

仅 JDK 18+(JEP 418)。JDK-8 反证(temurin jdk8u372-b07):

```
$ javap -public java.net.spi.InetAddressResolver
Error: class not found: java.net.spi.InetAddressResolver
```

不做 JDK-8 镜像。
