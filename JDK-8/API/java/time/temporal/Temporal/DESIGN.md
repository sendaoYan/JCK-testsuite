# java.time.temporal.Temporal 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
接口：`isSupported(TemporalUnit)`、`with(TemporalField,long)/with(TemporalAdjuster)`、`plus(TemporalAmount)/plus(long,TemporalUnit)`、`minus.../until(Temporal,TemporalUnit)`。

## 2. Java 用户如何使用
```java
LocalDate d = LocalDate.of(2024, 6, 15).plus(1, ChronoUnit.WEEKS);
long days = d.until(LocalDate.of(2024,6,20), ChronoUnit.DAYS);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 用 LocalDate/LocalTime/Instant 实现类驱动：isSupported 单位矩阵、plus/minus 对称、until 精确差值、with(field) 钳制。

### 易错细节（JDK-8 实测锁定）
- until 返回带符号值（参数早于 this 为负）。
- 不支持的单位抛 UTE `Unsupported unit`。
