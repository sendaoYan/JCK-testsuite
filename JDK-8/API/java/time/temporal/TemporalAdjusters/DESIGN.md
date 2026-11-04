# java.time.temporal/TemporalAdjusters 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
静态工厂集合：`firstDayOfMonth/lastDayOfMonth/firstDayOfNextMonth/firstDayOfYear/lastDayOfYear/firstDayOfNextYear/firstInMonth/lastInMonth/dayOfWeekInMonth/next/nextOrSame/previous/previousOrSame`。

## 2. Java 用户如何使用
```java
LocalDate d = LocalDate.of(2024, 6, 15);
d.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)); // 2024-06-03
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 13 个工厂在固定日期上的完整期望表（含月末、跨年、`dayOfWeekInMonth(2,...)`/负数第几周）；next/nextOrSame 边界（同日本身）；闰年 2 月末。

### 易错细节（JDK-8 实测锁定）
- `nextOrSame(同日)` 返回原日期，`next(同日)` 返回下一周。
- `dayOfWeekInMonth(-1,...)` 等价 lastInMonth。
