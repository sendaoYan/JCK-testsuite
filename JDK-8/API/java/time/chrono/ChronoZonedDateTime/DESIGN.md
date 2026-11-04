# java.time.chrono.ChronoZonedDateTime 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
任意日历带时区日期时间接口：`from(TemporalAccessor)`、`timeLineOrder`、`toInstant/toEpochSecond/toLocalDate/getOffset/getZone/withZoneSameLocal/isAfter/isBefore/compareTo`。

## 2. Java 用户如何使用
```java
ChronoZonedDateTime<?> c = ChronoZonedDateTime.from(
    ZonedDateTime.of(2024,6,15,10,0,0,0, ZoneId.of("UTC")));
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- from(ZDT) 精确串 `2024-06-15T10:00Z[UTC]`；from(LocalDate/LDT) 的 DTE 精确 message；toInstant/toEpochSecond；JapaneseDate.atTime().atZone() 组合；timeLineOrder 按 instant。

### 易错细节（JDK-8 实测锁定）
- from 失败的 message 是 `Unable to obtain ZonedDateTime from TemporalAccessor: ...`（对象 toString 内嵌）。
