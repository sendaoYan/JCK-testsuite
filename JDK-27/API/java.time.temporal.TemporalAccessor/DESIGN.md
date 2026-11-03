# TemporalAccessor 测试设计案

## 1. API 功能
`TemporalAccessor` 是日期/时间对象的只读基础接口，是所有 `java.time` 类型的父接口。
方法分组：
- 支持：`isSupported(TemporalField)`
- 取值：`getLong(TemporalField)`（long）、`get(TemporalField)`（int，默认）、`range(TemporalField)`（默认）
- 查询：`query(TemporalQuery<R>)`（默认）

## 2. Java 用户如何使用
```java
TemporalAccessor ta = LocalDate.of(2024,6,15);
boolean ok = ta.isSupported(ChronoField.DAY_OF_MONTH);
long year = ta.getLong(ChronoField.YEAR);
int dom = ta.get(ChronoField.DAY_OF_MONTH);
ValueRange r = ta.range(ChronoField.DAY_OF_MONTH);
LocalDate d = ta.query(TemporalQueries.localDate());
```
组合链路：`isSupported → get/getLong/range`，或直接 `query(TemporalQuery)` 提取任意类型信息。

## 3. 开源代码实际用法调研
- JDK 自身：`LocalDate`、`LocalTime`、`ZonedDateTime` 等全部 `implements TemporalAccessor`（间接经 `Temporal`）。
- 权威实现：`src/java.base/share/classes/java/time/temporal/TemporalAccessor.java`（`get` 默认 `Math.toIntExact(getLong(field))`；`range` 默认若字段为 `ChronoField` 则抛 `UnsupportedTemporalTypeException`；`query` 默认 `query.queryFrom(this)`）。
- 格式化底层：`src/java.base/share/classes/java/time/format/Parsed.java` 与 `DateTimeFormatterBuilder` 以 `TemporalAccessor` 作为解析结果与取值目标。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalAccessorTest.testBlackBox()`）：
- `LocalDate` 支持 `DAY_OF_MONTH`/`YEAR`、不支持 `MINUTE_OF_HOUR`
- `getLong(YEAR)=2024`、`get(DAY_OF_MONTH)=15`
- `range(DAY_OF_MONTH)` 在 2024-06 为 1..30
- `query(TemporalQueries.localDate())` 返回原日期
- `LocalTime` 取 `MINUTE_OF_HOUR=30`、不支持 `YEAR`、`precision()==NANOS`
白盒部分（`testWhiteBox()`）：`isInterface`、无父接口、`getLong` 返回 long、`get` 返回 int。
