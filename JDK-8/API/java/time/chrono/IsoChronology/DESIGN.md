# java.time.chrono.IsoChronology 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
ISO-8601 日历：id `ISO`、calendarType `iso8601`；date/localDateTime/zonedDateTime/eraOf/isLeapYear/period。

## 2. Java 用户如何使用
```java
IsoChronology.INSTANCE.dateYearDay(2024, 167); // 2024-06-15
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 工厂矩阵（date/dateYearDay/dateEpochDay/localDateTime/zonedDateTime）；闰年 4/100/400 规则；eraOf(0/1)；period 无前缀 toString `P1Y2M3D`。

### 易错细节（JDK-8 实测锁定）
- `localDateTime(null)` NPE message 是 `temporal`。
- zonedDateTime 工厂吃 (Instant, ZoneId) 而非 LocalDateTime。
