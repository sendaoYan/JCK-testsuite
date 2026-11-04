# java.time.LocalTime 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(int,int[,int[,int]])`、`ofSecondOfDay(long)`、`ofNanoOfDay(long)`、`ofInstant(Instant,ZoneId)`、`parse(CharSequence[,Formatter])`、`now([ZoneId|Clock])`、`from`。
- 查询：`getHour/getMinute/getSecond/getNano`、`range/get/getLong`。
- 核心操作：`plusHours/plusMinutes/plusSeconds/plusNanos`、`minus*`、`withHour/withMinute/withSecond/withNano`、`with(TemporalAdjuster|TemporalField,long)`、`truncatedTo(TemporalUnit)`、`until(...)`。
- 转换：`atDate(LocalDate)`、`atOffset(ZoneOffset)`、`toSecondOfDay/toNanoOfDay`、`toEpochSecond(LocalDate,ZoneOffset)`、`format`。
- 比较/对象：`isBefore/isAfter`、`compareTo`、`equals/hashCode/toString`。
- 常量：`MIN/MAX/MIDNIGHT/NOON`。

## 2. Java 用户如何使用
```java
LocalTime t = LocalTime.of(23, 59, 30);
LocalTime next = t.plusHours(2);      // 00:59:30（跨午夜回绕）
LocalTime rounded = t.truncatedTo(ChronoUnit.HOURS); // 23:00
LocalDateTime dt = t.atDate(LocalDate.of(2024, 3, 1));
int secs = t.toSecondOfDay();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKLocalTime.java`（of/plus/minus/with/truncatedTo/atOffset 契约）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`（LocalTime 与 java.sql.Time 互转）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/LocalTime.java`（内部自用：`plusHours` 用 `LocalTime.of(...)` 回绕）。

## 4. 测试用例设计 + 自动化实现
对应 `LocalTimeTest.java` 的 `testBlackBox()`：
- 工厂/查询、跨午夜 plus/minus 回绕、with/truncatedTo、toSecondOfDay/toNanoOfDay、atDate/atOffset、比较、越界（of(24,0) 等）与 parse 非法。
- 确定性：固定时间、无墙钟；headless-safe。
