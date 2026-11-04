# java.time.chrono.ChronoPeriod 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
任意日历期间接口：静态 `between(ChronoLocalDate,ChronoLocalDate)`；`get/getUnits/getChronology/negated/multipliedBy/plus/minus/isZero/isNegative`。

## 2. Java 用户如何使用
```java
ChronoPeriod p = ChronoPeriod.between(LocalDate.of(2023,6,15), LocalDate.of(2024,6,15));
p.toString(); // P1Y
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 各日历 period 的 toString（带日历前缀 `Japanese P1Y2M3D`，ISO 无前缀 `P1Y`）；between 月末钳制（1-31→3-01 = P1M1D）；negated/multipliedBy/plus/minus 算术；between 允许跨日历；null 参数 NPE 精确名 `startDateInclusive`/`endDateExclusive`。

### 易错细节（JDK-8 实测锁定）
- ISO 的 ChronoPeriod toString 是 `P1Y`（尾随零单位省略），日历型是 `Minguo P1Y3M3D`。
- between 可跨日历（按时间线换算）。
