# java.time.chrono.ChronoLocalDateTime 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
任意日历日期时间接口：`from(TemporalAccessor)`、`timeLineOrder`、`atZone/toInstant/toEpochSecond/toLocalDate/toLocalTime/isAfter/isBefore/isEqual`。

## 2. Java 用户如何使用
```java
ChronoLocalDateTime<?> c = ChronoLocalDateTime.from(LocalDateTime.of(2024,6,15,10,0));
c.atZone(ZoneId.of("UTC")).toString(); // 2024-06-15T10:00Z[UTC]
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- from(LDT) 直通；from(LocalDate) 抛 DTE `Unable to obtain LocalDateTime from TemporalAccessor: ...`；atZone 用 **ZoneId（区域）**时 toString 带 `[UTC]` 后缀、用 ZoneOffset 时不带；toEpochSecond/toInstant 精确值；timeLineOrder 跨日历比较。

### 易错细节（JDK-8 实测锁定）
- atZone 的参数用 `ZoneId.of("UTC")` 与 `ZoneOffset.UTC` 的 toString 输出不同（前者是 ZoneRegion）。
- from(null) NPE `temporal`。
