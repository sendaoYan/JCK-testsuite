# java.time.temporal.JulianFields 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
儒略日字段：`JULIAN_DAY/MODIFIED_JULIAN_DAY/RATA_DIE`。

## 2. Java 用户如何使用
```java
long j = LocalDate.of(2024, 6, 15).getLong(JulianFields.JULIAN_DAY);
LocalDate back = JulianFields.JULIAN_DAY.adjustInto(LocalDate.of(1970,1,1), 2440476L + 5);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 三字段与 EPOCH_DAY 的线性换算（JULIAN_DAY=EPOCH_DAY+2440588、MODIFIED_JULIAN_DAY=EPOCH_DAY-40587、RATA_DIE=EPOCH_DAY+719468）往返；range；对 LocalTime 抛 UTE。

### 易错细节（JDK-8 实测锁定）
- `JULIAN_DAY.adjustInto` 往返换算精度差 0；对 LocalTime 的 message 是 `Unsupported field: EpochDay`（内部归一化）。
