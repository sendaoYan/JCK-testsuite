# java.time.zone.ZoneRules

## 1. API 功能
一个时区在任意时刻的偏移规则集合：固定偏移或含历史/未来转换。`final`、`Serializable`，通过 `ZoneId.getRules()` 或静态工厂获得。

- 工厂：`static of(ZoneOffset)`（固定偏移）、`static of(ZoneOffset, ZoneOffset, List<ZoneOffsetTransition>, List<ZoneOffsetTransition>, List<ZoneOffsetTransitionRule>)`
- 查询偏移：`getOffset(Instant)`、`getOffset(LocalDateTime)`、`getValidOffsets(LocalDateTime)`、`getStandardOffset(Instant)`
- DST：`getDaylightSavings(Instant)`、`isDaylightSavings(Instant)`
- 转换：`getTransition(LocalDateTime)`、`nextTransition(Instant)`、`previousTransition(Instant)`、`getTransitions()`、`getTransitionRules()`
- 其他：`isFixedOffset()`、`isValidOffset(LocalDateTime, ZoneOffset)`、`equals`、`hashCode`、`toString`

## 2. Java 用户如何使用
```java
ZoneRules rules = ZoneId.of("UTC").getRules();
rules.isFixedOffset();                       // true
rules.getOffset(Instant.EPOCH);              // Z
rules.getValidOffsets(LocalDateTime.of(2024,6,1,0,0)); // [Z]
ZoneRules sh = ZoneId.of("Asia/Shanghai").getRules();
sh.getOffset(Instant.EPOCH);                 // +08:00
sh.getTransitionRules().isEmpty();           // true（中国无 DST）
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/ZonedDateTime.java`：第 376 行 `ZoneRules rules = zone.getRules();` 后用 `validOffsets` 解析本地时间。
- `jdk25u-dev/src/java.base/share/classes/java/time/ZoneId.java`：`getRules()` 抽象方法，`ZoneRegion`/`ZoneOffset` 各自实现。
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/ZoneRulesProvider.java`：`getRules(String, boolean)` 返回缓存规则。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/TCKZoneRules.java`：TCK 覆盖固定/转换规则、next/previousTransition、getValidOffsets（权威来源）。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `final`、非接口/抽象/枚举、父类 `Object`、`Serializable`、存在 `of(ZoneOffset)` 与 5 参 `of`（`ZoneOffset, ZoneOffset, List, List, List`）。
- 黑盒（UTC）：`isFixedOffset`、`getOffset/getStandardOffset`==UTC（用 `ZoneOffset.UTC.equals` 比较，避免 `ZoneRegion.equals` 陷阱）、transitions/rules 空、`getDaylightSavings`==ZERO、`isDaylightSavings` false、`getValidOffsets`==[Z]、`nextTransition/previousTransition` null。
- 黑盒（Asia/Shanghai）：epoch 偏移 +08:00、标准偏移 +08:00、非固定、`getTransitionRules()` 空（无 DST）、`getTransitions()` 非空（历史转换）。
- 黑盒（工厂）：`ZoneRules.of(ZoneOffset.ofHours(2))` 固定偏移。
- 全部固定输入、无墙钟、无全局状态、headless-safe。

- **深化补充（契约级）**：Berlin gap/overlap/常态三分（gap：validOffsets 空 + transition 非空；**overlap：两个 offsets 且 getTransition 也非 null**；常态一个 offset + transition null）；next/previousTransition 精确串；`getTransitions().size()==63`、`getTransitionRules()` 2 条末位规则精确串（`... at 01:00 UTC ...`）；`getDaylightSavings` PT1H；`ZoneRules.of(null)` 1 参 NPE `offset`、5 参 NPE `baseStandardOffset`；固定规则 toString `ZoneRules[currentStandardOffset=+01:00]`。
