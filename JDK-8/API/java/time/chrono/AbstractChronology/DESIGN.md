# java.time.chrono.AbstractChronology 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
Chronology 抽象实现：`compareTo/equals/hashCode/resolveDate/dateEpochDay/dateYearDay/dateNow/date` 等由具体日历复用。

## 2. Java 用户如何使用
```java
Chronology c = Chronology.of("Japanese");
c.dateEpochDay(19889L);  // Japanese Reiwa 6-06-15
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- compareTo 按 id 字典序（Hijrah-umalqura < ISO < Japanese < Minguo < ThaiBuddhist，Japanese vs ThaiBuddhist = -10）；equals/hashCode；dateNow(null ZoneId/Clock) NPE 精确参数名；date(裸 Instant) DTE 精确 message；resolveDate 部分字段返回 null、全字段成日期。

### 易错细节（JDK-8 实测锁定）
- `date(Instant)` 不能直接吃 Instant（缺 offset），message 是 `Unable to obtain LocalDate from TemporalAccessor: ... of type java.time.Instant`。
- resolveDate 的 map 未含全部字段时返回 null 而不是抛异常。
