# java.time.chrono.MinguoChronology 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
民国历：id `Minguo`、calendarType `roc`；eraOf/isLeapYear/period/dateNow/displayName。

## 2. Java 用户如何使用
```java
MinguoDate d = MinguoDate.of(113, 6, 15);  // ROC 113 = 2024
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- id/calendarType；闰年与 ISO 同构（113 闰/112 平）；eraOf(0/1/2 越界)；dateEpochDay/dateYearDay/dateNow(fixed)；displayName `Minguo Calendar`。

### 易错细节（JDK-8 实测锁定）
- 年份 = ISO 年 - 1911（1912=ROC 1）。
- proleptic YEAR 与 YEAR_OF_ERA 相等（ROC 用正年）。
