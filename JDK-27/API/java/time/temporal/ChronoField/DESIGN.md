# ChronoField 测试设计案

## 1. API 功能
`ChronoField` 是 `java.time.temporal.TemporalField` 的标准枚举实现，定义了 ISO-8601 日历与时间的所有标准字段（30 个常量），分为日期字段（YEAR/MONTH_OF_YEAR/DAY_OF_MONTH/DAY_OF_WEEK…）与时间字段（HOUR_OF_DAY/MINUTE_OF_HOUR/SECOND_OF_MINUTE…）。
核心方法分组：
- 查询：`getBaseUnit()`、`getRangeUnit()`、`range()`、`isDateBased()`、`isTimeBased()`
- 取值/设值：`getFrom(TemporalAccessor)`、`adjustInto(Temporal,long)`、`rangeRefinedBy`
- 校验：`checkValidValue(long)`、`checkValidIntValue(long)`
- 枚举标准：`values()`、`valueOf(String)`、`toString()`、`getDisplayName(Locale)`

## 2. Java 用户如何使用
用户直接引用静态常量（如 `ChronoField.MONTH_OF_YEAR`）并把它作为 `TemporalField` 使用：
```java
long m = ChronoField.MONTH_OF_YEAR.getFrom(LocalDate.of(2024, 3, 1)); // 3
LocalDate d = ChronoField.MONTH_OF_YEAR.adjustInto(LocalDate.of(2024,3,1), 6); // 2024-06-01
if (ChronoField.YEAR.isDateBased()) { ... }
ChronoField.MONTH_OF_YEAR.checkValidValue(13); // throws DateTimeException
```
典型组合链路：`range() → isValidValue`，或 `isSupportedBy(date) → getFrom(date) → adjustInto(date, v)`。

## 3. 开源代码实际用法调研
- JDK 自身：`src/java.base/share/classes/java/time/LocalDate.java`（第 65–75 行）静态导入 `ChronoField.DAY_OF_MONTH/YEAR/EPOCH_DAY/MONTH_OF_YEAR/...`，在 `range/getLong/with/plus` 内部按字段分支处理。
- 字段常量定义（权威范围值）：`src/java.base/share/classes/java/time/temporal/ChronoField.java` 第 250 行 `MINUTE_OF_HOUR(..., MINUTES, HOURS, ValueRange.of(0, 59))`、第 405 行 `DAY_OF_MONTH(..., DAYS, MONTHS, ValueRange.of(1, 28, 31))`、第 479 行 `MONTH_OF_YEAR(..., MONTHS, YEARS, ValueRange.of(1, 12))`、第 558 行 `YEAR(..., YEARS, FOREVER, ValueRange.of(Year.MIN_VALUE, Year.MAX_VALUE))`。
- JDK 测试：`test/jdk/java/time/tck/java/time/TCKLocalDate.java`、`TCKLocalTime.java` 等大量使用 `ChronoField` 做逐字段断言。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`ChronoFieldTest.testBlackBox()`）：
- 常量范围：`MONTH_OF_YEAR=1..12`、`DAY_OF_MONTH=1..28/31`、`YEAR=-999999999..999999999`、`MINUTE_OF_HOUR=0..59`
- 日期/时间分类：`YEAR.isDateBased()` 真且 `isTimeBased()` 假；`HOUR_OF_DAY` 反之
- 基/范围单位：`MONTH_OF_YEAR` 基 `MONTHS` 范围 `YEARS`；`DAY_OF_MONTH` 基 `DAYS` 范围 `MONTHS`
- 支持性：`MONTH_OF_YEAR` 支持 `LocalDate`、`HOUR_OF_DAY` 不支持
- 组合操作：`adjustInto` 改月、`getFrom` 取分钟、`DAY_OF_WEEK` 取周六(6)
- 校验与异常：`checkValidIntValue/checkValidValue` 合法返回原值、越界抛 `DateTimeException`
白盒部分（`testWhiteBox()`）：反射断言 `isEnum`/`final`/`extends Enum`/实现 `TemporalField`/30 常量/`valueOf` 往返。
