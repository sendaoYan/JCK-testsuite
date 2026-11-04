# java.time.Instant 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`ofEpochSecond(long[,long])`、`ofEpochMilli(long)`、`parse`、`now([Clock])`、`from`。
- 查询：`getEpochSecond/getNano`、`get/getLong/range`。
- 核心操作：`plusSeconds/plusMillis/plusNanos`、`plus(Duration/TemporalAmount)`、`plusSaturating(Duration)`（JDK-21+）、`minus*`、`truncatedTo`、`until(Instant|Temporal,unit)`、`with`。
- 转换：`atOffset(ZoneOffset)`、`atZone(ZoneId)`、`toEpochMilli`。
- 比较/对象：`isBefore/isAfter`、`compareTo`、`equals/hashCode/toString`。
- 常量：`EPOCH/MIN/MAX`。

## 2. Java 用户如何使用
```java
Instant i = Instant.ofEpochSecond(60, 500_000_000);
Instant later = i.plusSeconds(25);
Duration gap = i.until(later);
long ms = i.toEpochMilli();
ZonedDateTime zdt = i.atZone(ZoneOffset.UTC);
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：
```java
Instant inst = Instant.ofEpochSecond(secs, nanos);
```
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKInstant.java`（ofEpochSecond 纳秒归一化、plus/minus、compareTo）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/Instant.java`（`plusSeconds` 用 `Math.addExact`，`create` 做范围校验）。

## 4. 测试用例设计 + 自动化实现
对应 `InstantTest.java` 的 `testBlackBox()`：
- 工厂/查询、纳秒归一化（`ofEpochSecond(0,-1)` → -1s+999999999ns）、plus/minus 进位、until→Duration、atZone/atOffset、plusSaturating 饱和、比较。
- 异常：`ofEpochSecond(Long.MAX_VALUE,1e9)` 抛 ArithmeticException；`MAX.plusSeconds(1)` 抛 DateTimeException。
- 序列化：`serialVersionUID == -665713676816604388L`；MIN/MAX/EPOCH/负纳秒/普通值 round-trip（EPOCH/MIN/MAX 为缓存单例，仅断言 equals）。
- null 语义：`parse/from/now/plus/minus/with/until/atOffset/atZone` → NPE。
- 确定性：固定 Instant；headless-safe。
