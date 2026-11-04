# TemporalAdjuster 测试设计案

## 1. API 功能
`TemporalAdjuster` 是函数式接口，封装“从一个日期/时间对象生成另一个日期/时间对象”的调整逻辑。
- 唯一抽象方法：`Temporal adjustInto(Temporal temporal)`
`TemporalAdjusters` 提供大量现成 adjuster；用户也可用 lambda/method-ref 自定义。

## 2. Java 用户如何使用
```java
TemporalAdjuster firstOfMonth = t -> t.with(ChronoField.DAY_OF_MONTH, 1);
LocalDate d = (LocalDate) firstOfMonth.adjustInto(LocalDate.of(2024,6,15)); // 2024-06-01
LocalDate d2 = LocalDate.of(2024,6,15).with(firstOfMonth);                  // 等价
```
组合链路：`adjustInto` 返回 `Temporal`；`Temporal.with(TemporalAdjuster)` 内部调用它。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/TemporalAdjuster.java`（单方法接口）。
- JDK 测试：`test/jdk/java/time/tck/java/time/temporal/TCKTemporalAdjusters.java`（第 92 行 `TemporalAdjuster test = TemporalAdjusters.ofDateAdjuster(date -> date.plusDays(2))`；第 115 行 `(LocalDate) TemporalAdjusters.firstDayOfMonth().adjustInto(date)`）。
- 生态惯例：Spring/Quartz 等用 `TemporalAdjusters` 的调整器配合 `LocalDate.with` 实现“下周一/月末/季度首日”等业务日期计算。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalAdjusterTest.testBlackBox()`）：
- lambda adjuster（设 DAY_OF_MONTH=1）`adjustInto(2024-06-15)=2024-06-01`
- `LocalDate.with(TemporalAdjuster)` 得到同样结果
- 加一天 adjuster `adjustInto=2024-06-16`
- 恒等 adjuster 返回原值
白盒部分（`testWhiteBox()`）：`isInterface`、无父接口、`adjustInto` 返回 `Temporal`。
