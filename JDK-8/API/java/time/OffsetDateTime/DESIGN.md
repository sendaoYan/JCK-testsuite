# java.time.OffsetDateTime 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
值类 日期时间+偏移：工厂 `of/ofInstant/parse/from`、`toOffsetDateTime 之外的 toLocalDate/toLocalTime/toLocalDateTime/toInstant/toEpochSecond/atZoneSameInstant(不存在,用 toZonedDateTime)`、`withOffsetSameLocal/SameInstant`、`plus/minus/with/truncatedTo`、`compareTo/isAfter/isBefore/isEqual`、`format/query`。

## 2. Java 用户如何使用
```java
OffsetDateTime o = OffsetDateTime.parse("2024-06-15T10:15:30+02:00");
Instant i = o.toInstant();
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- parse 精确串；`2024-12-31T23:59:59.999999999Z` 纳秒边界；withOffsetSameInstant 平移时刻、SameLocal 保留墙钟；compareTo 与时区无关按 instant；`atZoneSameInstant/atZoneSimilarLocal` 在 JDK-8 只提供 `toZonedDateTime`/`atZone`——实测为 `toZonedDateTime()`，等墙钟换算断言 NY 实例 04:15:30-05:00。

### 易错细节（JDK-8 实测锁定）
- equals 比较 date+time+offset 三元组。
- compareTo 先比 instant 后比 offset（同一时刻不同偏移时 offset 大者大）。
