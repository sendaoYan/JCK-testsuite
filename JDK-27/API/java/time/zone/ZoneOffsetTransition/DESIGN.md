# java.time.zone.ZoneOffsetTransition

## 1. API 功能
一个"两个偏移量之间的不连续转换"，描述本地时间线上 gap（前进）或 overlap（回退）的瞬间。`final`、实现 `Comparable<ZoneOffsetTransition>` 和 `Serializable`，无公开构造器，只能用静态工厂。

- 工厂：`static of(LocalDateTime, ZoneOffset before, ZoneOffset after)`
- 查询：`getInstant()`、`toEpochSecond()`、`getDateTimeBefore()`、`getDateTimeAfter()`、`getOffsetBefore()`、`getOffsetAfter()`、`getDuration()`
- 判定：`isGap()`、`isOverlap()`、`isValidOffset(ZoneOffset)`
- 其他：`compareTo(...)`、`equals`、`hashCode`、`toString`

## 2. Java 用户如何使用
```java
ZoneOffsetTransition t = ZoneOffsetTransition.of(
    LocalDateTime.of(2024, 3, 31, 2, 0), ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
t.isGap();                // true（前进）
t.getDateTimeAfter();     // 03:00
t.getInstant();           // 2024-03-31T01:00Z
ZoneOffsetTransition o = ZoneOffsetTransition.of(
    LocalDateTime.of(2024, 10, 27, 3, 0), ZoneOffset.ofHours(2), ZoneOffset.ofHours(1));
o.isOverlap();            // true（回退）
o.isValidOffset(ZoneOffset.ofHours(1)); // overlap 时两个偏移都有效
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/ZoneRules.java`：`getTransition(LocalDateTime)`/`nextTransition`/`previousTransition` 内部维护并遍历 `ZoneOffsetTransition` 列表。
- `jdk25u-dev/src/java.base/share/classes/java/time/ZonedDateTime.java`：将本地时间通过 transition 解析出正确偏移。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/TCKZoneOffsetTransition.java`：TCK 对 `of`/`isGap`/`isOverlap`/`getValidOffsets`/序列化逐项断言（权威行为来源）。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/TCKZoneRules.java`：在真实 ZoneRules 中构造 gap/overlap 场景验证。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `final`、非接口/抽象/枚举、父类 `Object`、实现 `Comparable`+`Serializable`、存在 `of(LocalDateTime, ZoneOffset, ZoneOffset)`。
- 黑盒（gap = 2024-03-31T02:00 +1→+2）：断言 `getDateTimeBefore/After`、`getInstant`、`toEpochSecond`、`getOffsetBefore/After`、`getDuration`==1h、`isGap`/`isOverlap`、`isValidOffset`（gap 下两个偏移均无效，源自源码 `isGap() ? false : ...`）、`toString` 含 "Gap"。
- 黑盒（overlap = 2024-10-27T03:00 +2→+1）：`isOverlap`/`isGap`、`getDateTimeAfter`、`isValidOffset`（+1/+2 有效、+3 无效）、`toString` 含 "Overlap"。
- 组合：`compareTo`（gap 早于 overlap）、`equals`/`hashCode`（两个等价 gap 相等，与 overlap 不等）。
- 全部固定输入，无墙钟、无全局状态、headless-safe。

- **深化补充（契约级）**：锁定 Berlin 2024 gap/overlap 全字段（gap instant `2024-03-31T01:00:00Z`/duration PT1H、overlap duration **PT-1H**）、`toEpochSecond()==1711846800`、isValidOffset 矩阵（gap 全 false、overlap 恰好 before/after 两个 true）、`of(null)` NPE `transition`、`of(相同偏移)` IAE `Offsets must not be equal`。
