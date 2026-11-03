# TemporalField 测试设计案

## 1. API 功能
`TemporalField` 是日期/时间“字段”的接口，`ChronoField`、`IsoFields` 字段、`WeekFields` 字段都实现它。
方法分组：
- 查询：`getBaseUnit()`、`getRangeUnit()`、`range()`、`isDateBased()`、`isTimeBased()`
- 支持/细化范围：`isSupportedBy(TemporalAccessor)`、`rangeRefinedBy(TemporalAccessor)`
- 取值/设值：`getFrom(TemporalAccessor)`、`adjustInto(Temporal,long)`
- 默认：`getDisplayName(Locale)`、`resolve(Map, TemporalAccessor, ResolverStyle)`
- `toString()`

## 2. Java 用户如何使用
```java
TemporalField f = ChronoField.DAY_OF_MONTH;
boolean dateBased = f.isDateBased();
TemporalUnit base = f.getBaseUnit();        // DAYS
ValueRange r = f.range();                   // 1..28/31
ValueRange rr = f.rangeRefinedBy(LocalDate.of(2020,2,1)); // max 29
long v = f.getFrom(LocalDate.of(2020,1,15));
LocalDate d = f.adjustInto(LocalDate.of(2020,1,1), 20);
```
组合链路：`isSupportedBy → rangeRefinedBy/getFrom/adjustInto`。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/TemporalField.java`（`getDisplayName` 默认 `return toString()`；`resolve` 默认 `return null`）。
- JDK 自身：`ChronoField` 实现该接口（`src/java.base/share/classes/java/time/temporal/ChronoField.java`）；`LocalDate.range/getLong/with` 以 `TemporalField` 为入参逐字段分支。
- 格式化底层：`DateTimeFormatterBuilder`/`Parsed` 用 `TemporalField` 定义可打印/解析的字段并做 `resolve`。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalFieldTest.testBlackBox()`，用 `ChronoField.DAY_OF_MONTH`/`MONTH_OF_YEAR` 具体化）：
- 分类/支持：`isDateBased` 真、`isTimeBased` 假；支持 `LocalDate`、不支持 `LocalTime`
- 单位：`DAY_OF_MONTH` 基 `DAYS` 范围 `MONTHS`；`MONTH_OF_YEAR` 基 `MONTHS` 范围 `YEARS`
- 范围/细化：`range()=1..28/31`；`rangeRefinedBy(2020-02)=max 29`、`(2021-02)=max 28`
- 取值/设值：`getFrom=15`、`adjustInto=01-20`；`MONTH_OF_YEAR.getFrom=7`
- `getDisplayName(Locale.US)` 非空
白盒部分（`testWhiteBox()`）：`isInterface`、11 个关键方法存在、`getFrom` 返回 long。
