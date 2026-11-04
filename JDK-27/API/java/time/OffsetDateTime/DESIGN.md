# java.time.OffsetDateTime 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(...)`、`of(LocalDate,LocalTime,ZoneOffset)`、`of(LocalDateTime,ZoneOffset)`、`ofInstant(Instant,ZoneId)`、`parse`、`now`、`from`。
- 查询：`getYear/getMonth/getDayOfMonth/getHour/getMinute/getSecond/getNano`、`getOffset`、`get(...)`、`toLocalDate/toLocalTime/toLocalDateTime/toOffsetTime/toZonedDateTime`。
- 核心操作：`plus*`、`minus*`、`with*`、`withOffsetSameLocal/withOffsetSameInstant`、`truncatedTo`、`until`。
- 转换：`toInstant/toEpochSecond`、`atZoneSameInstant/atZoneSimilarLocal`、`format`。
- 比较/对象：`isBefore/isAfter/isEqual`、`compareTo`、`timeLineOrder()`（按绝对时刻比较）、`equals/hashCode/toString`。
- 常量：`MIN/MAX`。

## 2. Java 用户如何使用
```java
OffsetDateTime odt = OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2));
Instant instant = odt.toInstant();                    // 08:30Z
OffsetDateTime sameLocal = odt.withOffsetSameLocal(ZoneOffset.ofHours(-3)); // 10:30-03:00
int cmp = OffsetDateTime.timeLineOrder().compare(odt, sameLocal); // 按绝对时刻
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKOffsetDateTime.java`（of/withOffsetSame*/toInstant/timeLineOrder 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/OffsetDateTime.java`（内部自用）。

## 4. 测试用例设计 + 自动化实现
对应 `OffsetDateTimeTest.java` 的 `testBlackBox()`：
- 工厂/查询、toInstant/toZonedDateTime/toOffsetTime、withOffsetSameLocal/SameInstant、timeLineOrder（不同偏移下按绝对时刻比较，a=10:30+02:00=08:30Z、earlier=08:00Z、later=09:30Z）、比较、越界与 parse 非法、不可变性。
- 确定性：固定偏移 + 固定时刻；headless-safe。
