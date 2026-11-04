# Temporal 测试设计案

## 1. API 功能
`Temporal` 是日期/时间对象的读-写接口，继承 `TemporalAccessor`，在只读基础上增加修改能力。
方法分组：
- 修改：`with(TemporalAdjuster)`、`with(TemporalField,long)`
- 加减：`plus(TemporalAmount)`、`plus(long,TemporalUnit)`、`minus(TemporalAmount)`、`minus(long,TemporalUnit)`
- 差值：`until(Temporal,TemporalUnit)`
- 支持：`isSupported(TemporalUnit)`（另有继承的 `isSupported(TemporalField)`）

## 2. Java 用户如何使用
`LocalDate`、`LocalDateTime`、`ZonedDateTime`、`Instant` 等都实现 `Temporal`：
```java
Temporal t = LocalDate.of(2024,6,15);
t = t.with(ChronoField.YEAR, 2025);
t = t.plus(1, ChronoUnit.DAYS);
t = t.plus(Period.ofDays(3));
t = t.with(ta -> ta.with(ChronoField.DAY_OF_MONTH, 1));
long d = t.until(LocalDate.of(2024,6,20), ChronoUnit.DAYS);
```
组合链路：`isSupported → with/plus/minus → until`。

## 3. 开源代码实际用法调研
- JDK 自身：`src/java.base/share/classes/java/time/LocalDate.java` 声明 `public final class LocalDate ... implements Temporal, TemporalAdjuster, ...`；第 1626 行 `public long until(Temporal endExclusive, TemporalUnit unit)` 对 `ChronoUnit` 分支计算。
- `Period`/`Duration` 的 `addTo(Temporal)`/`subtractFrom(Temporal)` 接收 `Temporal` 参数（`src/java.base/share/classes/java/time/Period.java` 第 272 行、`Duration.java` 第 339 行循环 `amount.getUnits()`）。
- 生态惯例：任何日期工具方法（如 Spring 的日期运算封装）都把 `Temporal` 作为入参以兼容多种日期类型。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalTest.testBlackBox()`，用 `LocalDate` 作为具体实现）：
- `LocalDate` 是 `Temporal`；支持 `YEAR`/`DAYS`、不支持 `HOURS`
- `with(ChronoField.YEAR, 2025)`；`plus/minus(1, DAYS)`；`plus/minus(Period.ofDays(3))`
- `with(TemporalAdjuster)`（lambda 设 DAY_OF_MONTH=1）
- `until(2024-06-20, DAYS) == 5`
白盒部分（`testWhiteBox()`）：`isInterface`、继承 `TemporalAccessor`、`with/plus/until` 返回类型。
