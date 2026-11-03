# java.time.zone.ZoneRulesProvider

## 1. API 功能
抽象 SPI 基类，向系统提供时区规则（默认实现为 tzdb）。静态方法通过已注册 provider 查找规则。

- 静态查询：`getAvailableZoneIds()`、`getRules(String, boolean forCaching)`、`getVersions(String zoneId)`
- SPI 管理：`registerProvider(ZoneRulesProvider)`、`refresh()`

## 2. Java 用户如何使用
```java
ZoneRulesProvider.getAvailableZoneIds();          // Set<String>（如含 "UTC"/"Asia/Shanghai"）
ZoneRulesProvider.getRules("UTC", false);         // 固定偏移规则
ZoneRulesProvider.getRules("Asia/Shanghai", false).getOffset(Instant.EPOCH); // +08:00
ZoneRulesProvider.getVersions("UTC");             // NavigableMap<version, ZoneRules>
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/TzdbZoneRulesProvider.java`：JDK 内置 provider，加载 tzdb 并提供 `getAvailableZoneIds`/`getRules`/`getVersions`。
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/ZoneRulesProvider.java`：`registerProvider` 注册自定义 provider（本测试不调用，避免全局副作用）。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/TCKZoneRulesProvider.java`：TCK 覆盖 available ids、getRules、未知 id 抛异常。
- `jdk25u-dev/test/jdk/java/time/nontestng/java/time/zone/zoneProvider/custom/CustomZoneRulesProvider.java`：自定义 provider 示例。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `abstract`、非 final/接口/枚举、父类 `Object`、存在静态 `getRules`/`getAvailableZoneIds`/`getVersions`。
- 黑盒：`getAvailableZoneIds()` 拷贝进 `TreeSet`（排序后确定性断言）并断言含 `UTC`、`Asia/Shanghai`（不断言精确大小/内容）；`getRules("UTC", false)` 固定偏移且 epoch 偏移 ==UTC；`getRules("Asia/Shanghai", false)` epoch 偏移 ==+08:00；`getRules("UTC", true)`（forCaching）同样固定偏移；`getVersions("UTC")` 非空且首项规则非空。
- 异常：`getRules("Not/AZone", false)` 抛 `ZoneRulesException`。
- 不调用 `registerProvider`（避免全局状态污染）、无墙钟、headless-safe。
