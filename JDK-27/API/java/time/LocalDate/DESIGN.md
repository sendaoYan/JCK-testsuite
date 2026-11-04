# java.time.LocalDate 测试方案

## 1. API 功能（按方法分组，依据 JDK-27 `javap`）
- 构造/工厂：`of(int,int,int)`、`of(int,Month,int)`、`ofYearDay(int,int)`、`ofEpochDay(long)`、`ofInstant(Instant,ZoneId)`、`parse(CharSequence[,Formatter])`、`now([ZoneId|Clock])`、`from(TemporalAccessor)`。
- 查询：`getYear/getMonthValue/getMonth/getDayOfMonth/getDayOfYear/getDayOfWeek`、`isLeapYear`、`lengthOfMonth/lengthOfYear`、`getChronology/getEra`、`range/get/getLong`。
- 核心操作（算术）：`plusDays/plusWeeks/plusMonths/plusYears`、`minus*`、`withYear/withMonth/withDayOfMonth/withDayOfYear`、`with(TemporalAdjuster|TemporalField,long)`、`until(ChronoLocalDate)`、`until(date,ChronoUnit)`、`datesUntil(...)`。
- 转换：`atTime(LocalTime|int,int|int,int,int|int,int,int,int|OffsetTime)`、`atStartOfDay([ZoneId])`、`toEpochDay`、`toEpochSecond(LocalTime,ZoneOffset)`、`format(DateTimeFormatter)`。
- 比较/对象：`isBefore/isAfter/isEqual`、`compareTo`、`equals/hashCode/toString`。
- 常量：`MIN/MAX/EPOCH`。

## 2. Java 用户如何使用
```java
LocalDate d = LocalDate.of(2024, 1, 31);       // 工厂构造
LocalDate next = d.plusMonths(1);              // 2024-02-29（月末钳制）
LocalDate first = d.with(TemporalAdjusters.firstDayOfMonth());
LocalDateTime ldt = d.atTime(10, 15);          // 转日期时间
boolean leap = d.isLeapYear();                 // 查询
d.datesUntil(d.plusDays(3)).forEach(System.out::println); // 流式区间
```

## 3. 开源代码实际用法调研
来源（JDK 自带权威测试，未用 WebSearch，按回退规则标注）：
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`（LocalDate 相关链路的官方用法）。
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKLocalDate.java`（LocalDate TCK，覆盖 of/plus/minus/with/compareTo/until 全量契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/LocalDate.java`（内部自用：`plusMonths` 里用 `LocalDate.of(...)` 做月末钳制）。
```java
// JavatimeTest.java 摘录：LocalDate 与 epoch 换算
LocalDateTime ldt = LocalDateTime.ofEpochSecond(secs, nanos, ZoneOffset.UTC);
```

## 4. 测试用例设计 + 自动化实现
推导场景（对应 `LocalDateTest.java` 的 `testBlackBox()`）：
- 正常/工厂：of/ofYearDay/ofEpochDay/parse/toString/常量。
- 组合：of→plusDays/Months/Years→with→atTime→比较→until/datesUntil。
- 边界：闰年（1900/2000/2024）、月末钳制（1/31+1月）、ofEpochDay=0。
- 异常：of(2,30)/of(13,1)/parse 非法/plusYears 溢出。
- 不可变性：每次运算返回新实例、原实例不变。
- 序列化：`serialVersionUID == 2942565459149668126L`；MIN/MAX/负年份/普通日期 round-trip 且反序列化为新实例。
- null 语义：`parse/from/now(Clock|ZoneId)/plus/minus/with/until/isAfter/isBefore/isEqual/atTime/atStartOfDay/format/query/compareTo` 全部 NPE。
- temporal 访问器：`isSupported/range/get/getLong/query`，`get(SECOND_OF_DAY)` → UnsupportedTemporalTypeException。
- 确定性：固定日期、`ZoneOffset.UTC`、无墙钟、无随机、无集合迭代顺序依赖；headless-safe（无 `@key`）。
