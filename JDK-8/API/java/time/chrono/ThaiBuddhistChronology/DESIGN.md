# java.time.chrono/ThaiBuddhistChronology 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
佛历：id `ThaiBuddhist`、calendarType `buddhist`；eraOf/isLeapYear/period/dateNow/displayName。

## 2. Java 用户如何使用
```java
ThaiBuddhistDate d = ThaiBuddhistDate.of(2567, 6, 15); // BE 2567 = 2024
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- id/calendarType；eraOf(0/1)；isLeapYear(2567 闰)；dateEpochDay/dateNow(fixed) 精确串；displayName `Buddhist Calendar`。

### 易错细节（JDK-8 实测锁定）
- 年份 = ISO 年 + 543。
- 与 Minguo 一样 proleptic year 直通。
