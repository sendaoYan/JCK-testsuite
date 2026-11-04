# java.time.LocalDate 测试方案

## 1. API 功能（依据 JDK-8 `javap`，注意无 JDK 9+ 的 datesUntil/ofInstant/toEpochSecond/EPOCH）
- 工厂：`of(int,int,int)`、`of(int,Month,int)`、`ofYearDay`、`ofEpochDay`、`from`、`parse([Formatter])`、`now([ZoneId|Clock])`。
- 查询：`getYear/getMonthValue/getMonth/getDayOfMonth/getDayOfYear/getDayOfWeek/isLeapYear/lengthOfMonth/lengthOfYear/getChronology/getEra`。
- 时间访问器：`isSupported/range/get/getLong/query/adjustInto`。
- 算术：`plus/minus`（TemporalAmount|TemporalUnit|Years/Months/Weeks/Days）、`with`（TemporalAdjuster|TemporalField|Year/Month/DayOfMonth/DayOfYear）。
- `until(ChronoLocalDate)`→Period、`until(Temporal,TemporalUnit)`。
- 比较/对象：`isBefore/isAfter/isEqual/compareTo/equals/hashCode/toString`。
- 转换：`atTime(...)`、`atStartOfDay([ZoneId])`、`toEpochDay`、`format`。
- 常量：`MIN/MAX`。

## 2. Java 用户如何使用
```java
LocalDate d = LocalDate.of(2024, 1, 31);
LocalDate next = d.plusMonths(1);              // 2024-02-29 月末钳制
LocalDate first = d.with(TemporalAdjusters.firstDayOfMonth());
LocalDateTime ldt = d.atTime(10, 15);
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/TCKLocalDate.java`（of/plus/with/until 契约）。
- `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_time`（LocalDate 边界矩阵）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/LocalDate.java`（月末钳制、异常类型）。

## 4. 测试用例设计
- 工厂：of/ofYearDay/ofEpochDay/parse（含 MIN/MAX/负年份 round-trip）/from/now(固定 Clock)/常量。
- 查询：闰年（1900/2000/2024）、月末钳制、lengthOf*、getChronology/getEra（BCE）。
- 时间访问器：isSupported、range(YEAR/MONTH/DAY)、get/getLong(PROLEPTIC_MONTH/EPOCH_DAY)、query、adjustInto；
  不支持字段 → UnsupportedTemporalTypeException。
- 算术+不可变性：plus/minus/with 全重载 + 月末钳制 + 负值 + 原实例不变。
- until：Period 分解 + DAYS/MONTHS/YEARS/负号/同值。
- 比较：compareTo 反对称、equals/hashCode、equals(null)/异类型。
- 转换：atTime 全重载、atStartOfDay、toEpochDay（含 MIN）、format。
- 序列化：serialVersionUID(2942565459149668126L) + round-trip。
- 异常矩阵：of 非法日期/月/日/年溢出、ofYearDay、ofEpochDay 溢出、parse 非法、plusYears/withYear 溢出、
  plus(HOURS)/with(SECOND_OF_DAY) → UnsupportedTemporalTypeException。
- null 语义：of/parse/from/now/plus/minus/with/until/is*/atTime/atStartOfDay/format/query → NPE。
- 确定性：固定日期/Clock/UTC；headless-safe（无 `@key`）。
