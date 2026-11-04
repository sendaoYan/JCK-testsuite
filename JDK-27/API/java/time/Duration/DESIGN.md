# java.time.Duration 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`ofDays/ofHours/ofMinutes/ofSeconds(ofMillis/ofNanos)`、`of(long,TemporalUnit)`、`between(Temporal,Temporal)`、`parse`、`from`、`ofNanos/ofMillis/ofSeconds`（含负秒正纳秒归一化）、`ZERO`。
- 查询：`getSeconds/getNano`、`get(unit)`、`units()`、`toDays/toHours/toMinutes/toSeconds/toMillis/toNanos`、`toDaysPart/toHoursPart/toMinutesPart/toSecondsPart/toMillisPart/toNanosPart`、`isZero/isNegative`。
- 核心操作：`plus/plusDays/plusHours/plusMinutes/plusSeconds/plusMillis/plusNanos`、`minus*`、`multipliedBy/dividedBy`、`negated/abs`、`withSeconds/withNanos`、`truncatedTo`、`addTo/subtractFrom`、`toTotalDuration`、`spliterator/stream`（Java 21+）。
- 比较/对象：`compareTo`、`equals/hashCode/toString`。
- 常量：`ZERO`。

## 2. Java 用户如何使用
```java
Duration d = Duration.between(Instant.ofEpochSecond(0), Instant.ofEpochSecond(90));
long totalSeconds = d.getSeconds();
long minutesPart = d.toMinutesPart();
Duration neg = d.negated();
Duration twice = d.multipliedBy(2);
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKDuration.java`（ofSeconds/ofMillis 负归一化、multipliedBy/dividedBy、compareTo）。
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKClock_Tick.java`：
```java
Clock.tick(Clock.fixed(zdt.toInstant(), PARIS), Duration.ofMillis(250));
```
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/Duration.java`（`ofSeconds` 归一化、`parse` 用 regex）。

## 4. 测试用例设计 + 自动化实现
对应 `DurationTest.java` 的 `testBlackBox()`：
- 工厂（ofSeconds/ofMillis/ofNanos 负值归一化）、between、查询（toXxx/toXxxPart）、plus/minus/multipliedBy/dividedBy/negated/abs、比较、parse（ISO-8601）与非法、越界（ofNanos 溢出抛 ArithmeticException）。
- 确定性：固定 Duration/Instant；headless-safe。
