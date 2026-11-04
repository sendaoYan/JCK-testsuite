# ChronoUnit 测试设计案

## 1. API 功能
`ChronoUnit` 是 `java.time.temporal.TemporalUnit` 的标准枚举实现（16 个常量），从 `NANOS` 到 `FOREVER`。
核心方法分组：
- 时长：`getDuration()`、`isDurationEstimated()`
- 分类：`isDateBased()`、`isTimeBased()`、`isSupportedBy(Temporal)`
- 运算：`addTo(Temporal,long)`、`between(Temporal,Temporal)`
- 枚举标准：`values()`、`valueOf(String)`、`toString()`

## 2. Java 用户如何使用
```java
long days = ChronoUnit.DAYS.between(LocalDate.of(2024,1,1), LocalDate.of(2024,1,11)); // 10
LocalDate end = ChronoUnit.MONTHS.addTo(LocalDate.of(2024,1,31), 1); // 2024-02-29
Duration d = ChronoUnit.DAYS.getDuration(); // PT24H
boolean est = ChronoUnit.DAYS.isDurationEstimated(); // true（DST/月长差异）
```
组合链路：`isSupportedBy(t) → addTo/between`，或 `getDuration() → isDurationEstimated()`。

## 3. 开源代码实际用法调研
- 权威实现（时长/估计性判定）：`src/java.base/share/classes/java/time/temporal/ChronoUnit.java` 第 226–236 行 `isDurationEstimated()` 实现为 `return this.compareTo(DAYS) >= 0;`（即 DAYS 及以上为估算，因为夏令时导致天不等长）；第 240–254 行 `isDateBased()`/`isTimeBased()`。
- JDK 自身：`src/java.base/share/classes/java/time/LocalDate.java`（第 89 行 `import java.time.temporal.ChronoUnit`；第 1626 行 `until(...)` 对 `ChronoUnit` 分支实现 `DAYS/MONTHS/YEARS/...` 的差值计算）。
- 生态惯例：`ChronoUnit.DAYS.between` 是计算两个日期相差天数的标准写法，遍布 Spring/Quartz 等项目的日期工具类。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`ChronoUnitTest.testBlackBox()`）：
- 时长：`DAYS=86400s`、`HOURS=3600s`、`MINUTES=60s`
- 估计性（关键）：`DAYS.isDurationEstimated()` 真、`HOURS.isDurationEstimated()` 假
- 分类：`DAYS` 仅 date-based、`HOURS` 仅 time-based、`FOREVER` 两者皆否
- 运算：`MONTHS.addTo(2024-01-31, +1)=2024-02-29`；`DAYS.between=10`；`WEEKS.between=1`
- 支持性：`DAYS` 支持 `LocalDate`、`HOURS` 不支持
白盒部分（`testWhiteBox()`）：`isEnum`/`final`/`extends Enum`/实现 `TemporalUnit`/16 常量/`valueOf` 往返。
