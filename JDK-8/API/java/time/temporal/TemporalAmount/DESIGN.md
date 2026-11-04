# java.time.temporal.TemporalAmount 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
接口：`get(TemporalUnit)`、`getUnits()`、`addTo(Temporal)`、`subtractFrom(Temporal)`；实现类 Period/Duration。

## 2. Java 用户如何使用
```java
TemporalAmount p = Period.of(1, 2, 3);
p.get(ChronoUnit.DAYS);  // 3
LocalDate d = LocalDate.of(2024,1,1).plus(p);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- Period/Duration 的 getUnits 列表与 get 值；addTo/subtractFrom 对称；null unit 抛 UTE `Unsupported unit: null`。

### 易错细节（JDK-8 实测锁定）
- `Period.get(null)` 的 message 是 `Unsupported unit: null`（不是 NPE）。
- Duration.getUnits 只有 SECONDS/NANOS 两个。
