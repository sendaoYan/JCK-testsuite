# java.net.spi.InetAddressResolverProvider 设计文档

## 1. API 功能

提供 `InetAddressResolver` 实例的 SPI 抽象类，含嵌套 `Configuration`（sealed interface）。

- 抽象方法：`InetAddressResolver get(Configuration config)`、`String name()`。
- 构造器：protected 无参构造器。
- `Configuration`：`builtinResolver()`、`lookupLocalHostName()`；sealed，唯一（非 public）permitted 子类。

## 2. Java 用户如何使用

- 实现类：继承并实现 `get`/`name`，经 `META-INF/services` 注册；`name()` 标识 provider，
  `get(config)` 返回 resolver，可通过 `config.builtinResolver()` 委托内置解析器。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/net/spi/InetAddressResolverProvider.java`）：
  `Configuration` 是 sealed interface，permitted 子类为 `sun/net/ResolverProviderConfiguration`。

## 4. 测试用例设计 + 自动化实现

文件：`InetAddressResolverProviderTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / abstract / 父类 `Object` / protected 无参构造器 / `get`/`name` 方法面 / `Configuration` sealed。
- 黑盒：匿名实现 → `name()`/`get(null)`/`get(config)` 读回 resolver → `Configuration` 结构（sealed + 方法面）。
