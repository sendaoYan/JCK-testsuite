# TemporalQueries 测试设计案

## 1. API 功能
`TemporalQueries` 是常用 `TemporalQuery` 常量的静态工厂（不可实例化）：
- `zoneId()`（严格 ZoneId）、`zone()`（宽松，回退 offset）
- `chronology()`、`precision()`
- `offset()`、`localDate()`、`localTime()`
这些查询通过 `TemporalAccessor.query(...)` 或 `queryFrom(...)` 提取信息，无信息时返回 null（不抛异常）。

## 2. Java 用户如何使用
```java
Chronology chrono = TemporalQueries.chronology().queryFrom(LocalDate.now()); // ISO
TemporalUnit prec = TemporalQueries.precision().queryFrom(LocalDate.now());  // DAYS
ZoneId zone = TemporalQueries.zone().queryFrom(ZonedDateTime.now());
LocalDate d = LocalDate.now().query(TemporalQueries.localDate());
```
组合链路：`TemporalAccessor.query(TemporalQueries.xxx())` 是推荐写法（内部 `queryFrom`）。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/TemporalQueries.java`（javadoc 逐类型列出返回规则，如 `LocalDate→ISO chronology`、`precision→DAYS`、`LocalTime→NANOS`）。
- JDK 自身：`src/java.base/share/classes/java/time/format/Parsed.java` 第 223–242 行 `if (query == TemporalQueries.zoneId()) ... else if (query == TemporalQueries.chronology()) ...` 用 `==` 精确匹配这些常量。
- 其他：`DateTimeFormatterBuilder.java`、`DateTimePrintContext.java` 引用 `TemporalQueries`。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalQueriesTest.testBlackBox()`）：
- `localDate`：来自 `LocalDate`/`LocalDateTime`；`localTime`：来自 `LocalTime`/`LocalDateTime`
- `chronology`：`LocalDate` → `IsoChronology.INSTANCE`
- `zoneId`/`zone`：`ZonedDateTime`(UTC) → `ZoneId.of("UTC")`
- `offset`：`OffsetDateTime(+02:00)` 与 `ZoneOffset.ofHours(2)` → +02:00
- `precision`：`LocalDate→DAYS`、`LocalTime→NANOS`、`Instant→NANOS`
白盒部分（`testWhiteBox()`）：`final` 工具类、无公开构造器、7 个工厂非空。
