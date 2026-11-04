# java.time.Clock 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 抽象类：`Clock`（abstract），实现 `InstantSource`。
- 工厂：`systemUTC()`、`system(ZoneId)`、`systemDefaultZone()`、`fixed(Instant,ZoneId)`、`offset(Clock,Duration)`、`tick(Clock,Duration)`、`tickMillis/tickSeconds/tickMinutes(ZoneId)`。
- 查询：`instant()`、`millis()`、`getZone()`、`withZone(ZoneId)`、`equals/hashCode`。

## 2. Java 用户如何使用
```java
Clock fixed = Clock.fixed(Instant.ofEpochSecond(1000), ZoneOffset.UTC);
Instant i = fixed.instant();           // 固定时刻，可重复
Clock plus = Clock.offset(fixed, Duration.ofSeconds(100));
Clock ticking = Clock.tick(Clock.systemUTC(), Duration.ofSeconds(1));
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKClock_Tick.java`：
```java
Clock.tick(Clock.fixed(zdt.withNano(i * 1000_000).toInstant(), PARIS), Duration.ofMillis(250));
Clock.tick(Clock.systemUTC(), Duration.ofSeconds(0, 123));
```
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/Clock.java`（tick 对 duration<=0 抛 IllegalArgumentException）。

## 4. 测试用例设计 + 自动化实现
对应 `ClockTest.java` 的 `testBlackBox()`：
- systemUTC/systemDefaultZone/system(zone) 的 zone 与类型、fixed.instant 恒等、offset（100+3600→3700）、tick（对齐刻度/截断）、withZone、equals、无效 tick 时长（Duration.ofNanos(999)/负数）抛 IllegalArgumentException。
- 确定性：全部用 Clock.fixed，不依赖墙钟；headless-safe。
