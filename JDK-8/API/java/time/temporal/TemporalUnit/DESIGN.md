# java.time.temporal.TemporalUnit 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
接口：`getDuration/isDurationEstimated/isDateBased/isTimeSupported 系(实际为 isSupportedBy)/addTo/between`；实现 ChronoUnit/IsoFields.QUARTER_YEARS。

## 2. Java 用户如何使用
```java
TemporalUnit u = ChronoUnit.DECADES;
LocalDate d = LocalDate.of(2024,6,15).plus(5, u); // 2074
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 用 ChronoUnit 驱动接口方法契约；addTo/between 的类型适配与 UTE。

### 易错细节（JDK-8 实测锁定）
- `LocalDate.plus(5, DECADES)` 是 2074（10 年*5），不是 2029（易与 CENTURIES 混）。
