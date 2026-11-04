# java.time.OffsetTime 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
值类 时间+偏移：工厂 `of/ofInstant/parse/from`、`toLocalTime/getOffset`、`withOffsetSameLocal/SameInstant`、`plus/minus/with/truncatedTo`、`compareTo/isAfter/isBefore`、`format/query`。

## 2. Java 用户如何使用
```java
OffsetTime t = OffsetTime.parse("10:15:30+01:00");
OffsetTime u = t.withOffsetSameInstant(ZoneOffset.ofHours(2)); // 11:15:30+02:00
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- SameLocal/SameInstant 两个方向的精确串；truncatedTo 分钟/异常（FOREVER UTE）； compareTo 同墙钟不同偏移的次序（偏移大的排前）；DST gap 边界不适用（无日期）。

### 易错细节（JDK-8 实测锁定）
- `OffsetTime` 没有日期字段，`truncatedTo(DAYS)` 合法且得到 00:00（易误判为非法）。
