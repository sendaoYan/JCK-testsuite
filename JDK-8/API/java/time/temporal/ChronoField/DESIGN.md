# java.time.temporal.ChronoField 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
枚举，TemporalField 实现：NANO_OF_SECOND..ERA 共 30 个；`getBaseUnit/getRangeUnit/isDateBased/isTimeBased/range/isSupportedBy/getFrom/getLong/checkValidValue/checkValidIntValue/toString`。

## 2. Java 用户如何使用
```java
long v = LocalDate.of(2024, 6, 15).getLong(ChronoField.EPOCH_DAY);
ValueRange r = ChronoField.DAY_OF_MONTH.range();
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 30 个字段逐一精确 range 表（如 DAY_OF_MONTH `1 - 28/31`、YEAR_OF_DAY、EPOCH_DAY/INSTANT_SECONDS 的巨大区间按侧锁定）；isDateBased/isTimeBased 互斥矩阵；checkValidValue 越界 DTE 精确 message；对 LocalDate/LocalTime 各字段 isSupportedBy/getFrom。

### 易错细节（JDK-8 实测锁定）
- `ChronoField.values().length == 30`（JDK-8 与 27 相同）。
- EPOCH_DAY/INSTANT_SECONDS 的范围上界两版本不同——按侧锁定。
- MIN 侧越界 message 报 `Year`（LocalDate.MIN 减一天）而非 EpochDay。
