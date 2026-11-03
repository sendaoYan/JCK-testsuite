# java.time.OffsetTime 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(int,int,int,int,ZoneOffset)`、`of(LocalTime,ZoneOffset)`、`ofInstant(Instant,ZoneId)`、`parse`、`now`、`from`。
- 查询：`getHour/getMinute/getSecond/getNano`、`getOffset`、`get(...)`、`toLocalTime`。
- 核心操作：`plus*`、`minus*`、`with*`、`withOffsetSameLocal/withOffsetSameInstant`、`truncatedTo`、`until`。
- 转换：`atDate(LocalDate)`、`toEpochSecond(LocalDate,ZoneOffset)`、`format`。
- 比较/对象：`isBefore/isAfter/isEqual`、`compareTo`、`equals/hashCode/toString`。
- 常量：`MIN/MAX`。

## 2. Java 用户如何使用
```java
OffsetTime ot = OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(2));
OffsetTime shifted = ot.withOffsetSameInstant(ZoneOffset.ofHours(-3)); // 05:30-03:00
OffsetDateTime odt = ot.atDate(LocalDate.of(2024, 3, 1));
LocalTime local = ot.toLocalTime();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKOffsetTime.java`（of/withOffsetSame*/atDate/compareTo 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/OffsetTime.java`（内部自用）。

## 4. 测试用例设计 + 自动化实现
对应 `OffsetTimeTest.java` 的 `testBlackBox()`：
- 工厂/查询、toLocalTime/atDate/toEpochSecond、withOffsetSameLocal/SameInstant、比较（earlier=08:00Z、later=09:00Z 的绝对时刻语义）、越界与 parse 非法、不可变性。
- 确定性：固定偏移 + 固定时刻；headless-safe。
