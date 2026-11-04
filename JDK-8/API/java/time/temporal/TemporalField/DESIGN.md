# java.time.temporal.TemporalField 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
接口：`getBaseUnit/getRangeUnit/getRange/isDateBased/isTimeBased`；实现 ChronoField/ IsoFields/JulianFields/WeekFields 字段。

## 2. Java 用户如何使用
```java
TemporalField f = ChronoField.DAY_OF_MONTH;
f.isDateBased();
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 用 ChronoField 驱动五个方法的契约；对实现的 range 精确表（已在 ChronoField 覆盖，此处断言接口多态一致性）。

### 易错细节（JDK-8 实测锁定）
- isDateBased 与 isTimeBased 互斥（无两者皆 true 的字段）。
