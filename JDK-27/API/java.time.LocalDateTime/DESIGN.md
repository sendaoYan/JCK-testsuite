# java.time.LocalDateTime 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(int,...,int[,int])`、`of(LocalDate,LocalTime)`、`ofInstant(Instant,ZoneId)`、`ofEpochSecond(long,int,ZoneOffset)`、`parse`、`now`、`from`。
- 查询：`getYear/getMonthValue/getMonth/getDayOfMonth/getDayOfYear/getDayOfWeek`、`getHour/getMinute/getSecond/getNano`、`toLocalDate/toLocalTime`。
- 核心操作：`plusYears/Months/Weeks/Days/Hours/Minutes/Seconds/Nanos`、`minus*`、`with*`、`truncatedTo`、`until`。
- 转换：`atOffset(ZoneOffset)`、`atZone(ZoneId)`、`format`。
- 比较/对象：`isBefore/isAfter/isEqual`、`compareTo`、`equals/hashCode/toString`。
- 常量：`MIN/MAX`。

## 2. Java 用户如何使用
```java
LocalDateTime dt = LocalDateTime.of(2024, 1, 31, 23, 30);
LocalDateTime carry = dt.plusHours(2);        // 2024-02-01T01:30（跨日进位）
ZonedDateTime zdt = dt.atZone(ZoneId.of("UTC"));
Instant instant = zdt.toInstant();
LocalDate datePart = dt.toLocalDate();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：
```java
LocalDateTime ldt = LocalDateTime.ofEpochSecond(secs, nanos, ZoneOffset.UTC);
```
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKLocalDateTime.java`（plus/minus/with/atZone/atOffset/compareTo 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/LocalDateTime.java`（内部自用）。

## 4. 测试用例设计 + 自动化实现
对应 `LocalDateTimeTest.java` 的 `testBlackBox()`：
- 工厂/查询、跨日/月/年进位、月末钳制、toLocalDate/toLocalTime/atZone/atOffset（含 -03 偏移换算）、比较、越界与 parse 非法、不可变性。
- 确定性：固定日期时间 + `ZoneId.of("UTC")`/`ZoneOffset`；headless-safe。
