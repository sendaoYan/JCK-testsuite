# java.time.ZonedDateTime 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(...)`、`of(LocalDate,LocalTime,ZoneId)`、`of(LocalDateTime,ZoneId)`、`ofInstant(Instant,ZoneId)`、`ofInstant(LocalDateTime,ZoneOffset,ZoneId)`、`ofLocal(LocalDateTime,ZoneId,ZoneOffset)`、`ofStrict(...)`、`parse`、`now`、`from`。
- 查询：`getYear/getMonth/getDayOfMonth/getDayOfWeek/getHour/getMinute/getSecond/getNano`、`getOffset/getZone`、`get(...)`、`toLocalDate/toLocalTime/toLocalDateTime/toOffsetDateTime`。
- 核心操作：`plusYears/Months/Weeks/Days/Hours/Minutes/Seconds/Nanos`、`plus(Duration/TemporalAmount)`、`minus*`、`with*`（含 `withEarlierOffsetAtOverlap/withLaterOffsetAtOverlap`、`withZoneSameInstant/withZoneSameLocal/withFixedOffsetZone`、`withZoneSameLocal`）、`truncatedTo`、`until`。
- 转换：`toInstant/toEpochSecond`、`format`。
- 比较/对象：`isBefore/isAfter/isEqual`、`compareTo`、`equals/hashCode/toString`。
- 常量：无（final 值对象）。

## 2. Java 用户如何使用
```java
ZonedDateTime zdt = ZonedDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneId.of("UTC"));
Instant instant = zdt.toInstant();                 // 绝对时刻
ZonedDateTime shifted = zdt.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
ZonedDateTime sameClock = zdt.withZoneSameLocal(ZoneId.of("UTC")); // 保留本地时间
OffsetDateTime odt = zdt.toOffsetDateTime();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：
```java
ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
```
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKZonedDateTime.java`（of/ofInstant/withZoneSame*/plus/compareTo 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/ZonedDateTime.java`（内部自用）。

## 4. 测试用例设计 + 自动化实现
对应 `ZonedDateTimeTest.java` 的 `testBlackBox()`：
- 工厂/查询、toInstant/toOffsetDateTime/toLocal*、withZoneSameInstant/SameLocal、plus 跨区（含 UTC 与 Asia/Shanghai 固定偏移差）、比较（时刻 vs 本地时间语义）、parse（含纳秒 + 偏移）、越界与 parse 非法、不可变性。
- 确定性：固定 ZoneId（"UTC"/"Asia/Shanghai"）+ 固定时刻；headless-safe。
