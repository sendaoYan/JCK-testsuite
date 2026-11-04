# java.time.temporal.ChronoUnit 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
枚举，TemporalUnit 实现：NANOS..FOREVER 共 16 个；`getDuration/isDurationEstimated/isDateBased/isTimeBased/isSupportedBy/addTo/between/toString`。

## 2. Java 用户如何使用
```java
long d = ChronoUnit.DAYS.between(LocalDate.of(2024,1,1), LocalDate.of(2024,1,31)); // 30
Instant t = ChronoUnit.MINUTES.addTo(instant, 5);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 16 单位的 ISO 精确 duration 表（NANOS..ERAS 均固定，FOREVER isDurationEstimated=true 且 getDuration 是 Long.MAX 秒）；isDateBased/isTimeBased/estimated 三标志矩阵；between 跨类型 UTE 精确 message；addTo 在 Instant/LocalDate 上的行为。

### 易错细节（JDK-8 实测锁定）
- FOREVER.between(...) 永远抛 UTE/Unsupported。
- `NANOS.getDuration()` 是 `PT0.000000001S`。
- `MINUTES.between(LocalTime, LocalDate)` 抛 `UnsupportedTemporalTypeException`，message 指明类型不匹配。
