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

## 5. 深化缺口分析(批次 J,2026-11-05)

原 13 断言。深化到 81 断言:

- **形状精确化**(probed):唯一构造器 protected 无参(注意:无 name 参数,
  provider 名由抽象 `name()` 提供);恰 2 个声明抽象方法;`get` 参数
  Configuration/返回 InetAddressResolver/无 throws;无字段、非 final、非 sealed;
  无注解。
- **模块封装双门**(probed):`ctor.newInstance()` → IllegalAccessException;
  `setAccessible(true)` → InaccessibleObjectException(java.base 不 open
  java.net.spi)。抽象类数组可创建。
- **子类契约**:NamedProvider(构造器注入状态)记录 `name()`/`get()` 调用计数、
  null Configuration 逐次透传记录、per-instance 状态独立、多实例计数互不干扰。
- **组合链**:provider.get(null) → resolver.lookupByName 全链参数到达断言
  (host 字符串穿整条链);多态 provider 列表。
- **实现自由度**:`name()` 返回 null 不被拒、`get()` 返回 null 不被拒;
  子类抛 UOE/ISE 原样中继;toString 可覆写;带副作用的 get()(AtomicInteger)。
- Configuration 关系:嵌套/sealed/permitted=sun.net.ResolverProviderConfiguration
  (实测 public final,构造器 (InetAddressResolver, Supplier<String>),包未导出);
  ServiceLoader.load 可空载查询。专项见 `Configuration/DESIGN.md`(5 → 55)。
- 限制记录:**不真注册 provider**(需 ServiceLoader/module-info,超出批次范围;
  Configuration 实例因此不可获得,契约以形状 + 封装拒绝断言)。

## 6. 版本差异

仅 JDK 18+。JDK-8 反证(temurin jdk8u372-b07):

```
$ javap -public java.net.spi.InetAddressResolverProvider
Error: class not found: java.net.spi.InetAddressResolverProvider
```

不做 JDK-8 镜像。
