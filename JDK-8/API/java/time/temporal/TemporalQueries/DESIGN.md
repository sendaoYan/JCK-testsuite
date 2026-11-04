# java.time.temporal.TemporalQueries 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
常用 TemporalQuery 常量：`localDate/localTime/chronology/zone/zoneId/offset/precision`。

## 2. Java 用户如何使用
```java
LocalDate d = LocalDate.of(2024,6,15).query(TemporalQueries.localDate());
ZoneId z = ZonedDateTime.now(...).query(TemporalQueries.zoneId());
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 七个 query 在 LocalDate/LocalTime/LocalDateTime/Instant/OffsetDateTime/ZonedDateTime/Year 的期望表（含 null：Instant 对 localDate 返回 null）；precision 表（Nanos/Days/Eras/Auto）。null query 抛 NPE `query`。

### 易错细节（JDK-8 实测锁定）
- `TemporalQueries.localDate()` 查 ZonedDateTime 返回 LocalDate（非 null），查 Instant 返回 null——query 允许返回 null。
- `precision()` 返回 `ChronoUnit.NANOS` 等枚举而非字符串。
