# java.time.Clock 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
抽象类，时刻源+时区视图：`system([ZoneId])`、`fixed(Instant,ZoneId)`、`tick(Clock|InstantSource 不存在,Duration)`、`offset(Clock,Duration)`、`millis()`、`instant()`、`getZone()`、`withZone(ZoneId)`、`equals/hashCode/toString`。

## 2. Java 用户如何使用
```java
Clock c = Clock.fixed(Instant.parse("2024-06-15T10:15:30Z"), ZoneOffset.UTC);
c.instant();
Clock t = Clock.tick(c, Duration.ofMinutes(1));   // 整分对齐
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 全部用 fixed/tick/offset 派生钟断言，绝不触碰 system()/now() 的真实值。
- tick(fixed,0) 是恒等（JDK-8 实测不抛异常）；tick 负数抛 IAE `Tick duration must not be negative`。
- offset 钟只在 instant() 上加偏移，zone 不变。

### 易错细节（JDK-8 实测锁定）
- `Clock.tick(c, Duration.ZERO)` 不抛异常且等于原时刻（易误以为非法）。
- JDK-8 无 `InstantSource` 抽象（JDK-17+ 才抽出），`tickMinutes` 等便捷方法也不存在。
