# java.time.Period 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(int,int,int)`、`ofYears/ofMonths/ofWeeks/ofDays`、`between(LocalDate,LocalDate)`、`parse`、`from`、`ZERO`。
- 查询：`getYears/getMonths/getDays`、`getUnits()`、`get(unit)`、`isZero/isNegative`、`toTotalMonths()`。
- 核心操作：`plus/plusYears/plusMonths/plusDays`、`minus*`、`multipliedBy`、`negated`、`normalized`、`withYears/withMonths/withDays`、`addTo/subtractFrom`。
- 比较/对象：`equals/hashCode/toString`。
- 常量：`ZERO`。

## 2. Java 用户如何使用
```java
Period p = Period.between(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 4, 15)); // P2Y3M14D
Period np = p.normalized();          // 用 totalMonths 归一年月
LocalDate plus = p.addTo(LocalDate.of(2024, 1, 1));
Period neg = p.negated();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKPeriod.java`（between/of/normalized/negated/parse 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/Period.java`（内部自用）。

## 4. 测试用例设计 + 自动化实现
对应 `PeriodTest.java` 的 `testBlackBox()`：
- 工厂/查询、between、plus/minus/multipliedBy/negated/normalized、addTo/subtractFrom、parse（P2Y3M14D / P10D）、非法 parse 与越界、不可变性。
- 序列化：`serialVersionUID == -3587258372562876L` + ZERO/正/负/ofWeeks 四种值 round-trip。
- 空语义（NPE）：`parse`/`from`/`between`/`plus`/`minus`/`addTo`/`subtractFrom` 传 null。
- 确定性：固定日期；headless-safe。
