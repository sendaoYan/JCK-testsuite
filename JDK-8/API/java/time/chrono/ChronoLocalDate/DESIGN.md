# java.time.chrono.ChronoLocalDate 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
任意日历日期接口：`timeLineOrder` 比较器、`from(TemporalAccessor)`、`getChronology/ getEra/ isAfter/ isBefore/ isEqual/ compareTo/ toEpochDay/ format/ query`。

## 2. Java 用户如何使用
```java
ChronoLocalDate jp = JapaneseDate.of(2024, 6, 15);
ChronoLocalDate.timeLineOrder().compare(jp, LocalDate.of(2024,6,15)); // 0
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 跨日历 compareTo 按 chronology id 再比日期（jp vs mg = -3，jp vs iso-1d = 1）；timeLineOrder 纯按时间线（同日=0）；from(LocalDate/LocalDateTime/ZonedDateTime) 降级为 ISO；from(null) NPE `temporal`；isAfter/isBefore/isEqual 跨日历。

### 易错细节（JDK-8 实测锁定）
- **compareTo 与 timeLineOrder 结果可以不同**（前者先比日历 id）。
- `from(ZoneDateTime)` 得到的是 ISO LocalDate。
