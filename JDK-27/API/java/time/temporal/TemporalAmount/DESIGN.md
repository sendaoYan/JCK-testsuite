# TemporalAmount 测试设计案

## 1. API 功能
`TemporalAmount` 是“时间段/时间量”的接口，`Period`（年月日）与 `Duration`（秒/纳秒）实现它。
方法分组：
- 取值：`get(TemporalUnit)`、`getUnits()`
- 运算：`addTo(Temporal)`、`subtractFrom(Temporal)`

## 2. Java 用户如何使用
```java
TemporalAmount p = Period.of(1,2,3);
long days = p.get(ChronoUnit.DAYS);          // 3
List<TemporalUnit> units = p.getUnits();     // [YEARS, MONTHS, DAYS]
LocalDate d = (LocalDate) p.addTo(LocalDate.of(2024,1,1));

TemporalAmount dur = Duration.ofHours(2);
LocalTime t = (LocalTime) dur.addTo(LocalTime.of(1,0)); // 03:00
```
组合链路：`getUnits() → get(unit)`；`Temporal.plus(TemporalAmount)` 内部调用 `addTo`。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/Period.java`（第 435 行 `public long get(TemporalUnit unit)`；第 460 行 `getUnits()` 返回 `[YEARS, MONTHS, DAYS]`）；`Duration.java`（第 573 行 `getUnits()` 返回 `[SECONDS, NANOS]`）。
- JDK 自身：`Period.java` 第 272 行、`Duration.java` 第 339 行在 `addTo` 里 `for (TemporalUnit unit : amount.getUnits())` 逐单位累加。
- 生态惯例：任何接受“时间段”的框架（如 Quartz 间隔、缓存 TTL 运算）都通过 `TemporalAmount` 统一 `Period` 与 `Duration`。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalAmountTest.testBlackBox()`）：
- `Period.of(1,2,3)`：`getUnits()` 大小 3 且含 `YEARS/MONTHS/DAYS`；`get` 三项
- `Period.addTo/subtractFrom(2024-01-01)`、`Temporal.plus(Period)`
- `Duration.ofHours(2)`：`getUnits()` 大小 2 且含 `SECONDS/NANOS`；`get(SECONDS)=7200`、`get(NANOS)=0`
- `Duration.addTo/subtractFrom(LocalTime)` 跨午夜回绕
白盒部分（`testWhiteBox()`）：`isInterface`、`get` 返回 long、`getUnits` 返回 `List`。
