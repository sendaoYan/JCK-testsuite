# java.time.temporal.IsoFields 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
ISO-8601 扩展字段/单位：常量 `DAY_OF_QUARTER/QUARTER_OF_YEAR/WEEK_BASED_YEAR/WEEK_BASED_WEEK/WEEK_BASED_YEAR`（字段）与 `QUARTER_YEARS`（单位）。

## 2. Java 用户如何使用
```java
long q = LocalDate.of(2024, 6, 15).getLong(IsoFields.QUARTER_OF_YEAR); // 2
LocalDate d = LocalDate.of(2024,1,1).plus(1, IsoFields.QUARTER_YEARS);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 四个字段的 get/range 矩阵（DAY_OF_QUARTER `1 - 90/92` 随季度；WEEK_BASED_YEAR 随周历）；QUARTER_YEARS duration PT3M、plus 律动；无Chronology (非 ISO chronology 上 UTE)。

### 易错细节（JDK-8 实测锁定）
- QUARTER_OF_YEAR 在 `LocalDate` 上 `1 - 4`，DAY_OF_QUARTER 上界随所在季度天数（闰年 Q1 是 91）。
- 传给非 ISO 日历抛 UTE `Unsupported field`。
