# java.time.temporal/WeekFields 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
周定义：`of(Locale)/of(int,int)`、常量 `SUNDAY_START/ISO`、`dayOfWeek()/weekOfMonth()/weekOfYear()/weekBasedYear()/weekOfWeekBasedYear()`、`getFirstDayOfWeek/getMinimalDaysInFirstWeek/equals/toString`。

## 2. Java 用户如何使用
```java
WeekFields wf = WeekFields.ISO;
LocalDate.of(2024,1,1).get(wf.weekOfWeekBasedYear());
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- ISO/SUNDAY_START 两套的 firstDay/minimalDays；同一日期在两套下的 weekOfYear/dayOfWeek 值对照；自定义 of(DayOfWeek,1)。

### 易错细节（JDK-8 实测锁定）
- minimalDays=1 时每年第一周几乎总是 1；minimalDays 必须 1..7，否则 IAE `Minimal number of days is invalid`。
- `WeekFields.of(Locale.US)` 的 firstDay 是 SUNDAY。
