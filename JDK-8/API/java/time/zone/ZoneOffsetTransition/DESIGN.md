# java.time.zone.ZoneOffsetTransition 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
不可变转换点：`of(LocalDateTime,ZoneOffset,ZoneOffset)`、`getInstant/getDateTimeBefore/getDateTimeAfter/getOffsetBefore/getOffsetAfter/getDuration/isGap/isOverlap/isValidOffset/compareTo/equals/hashCode/toString/toEpochSecond`。

## 2. Java 用户如何使用
```java
ZoneOffsetTransition t = ZoneOffsetTransition.of(
    LocalDateTime.of(2024,3,31,2,0), ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
t.isGap();  // true
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- Berlin 2024 gap/overlap 两个实例的完整字段表（instant UTC 01:00Z、duration PT1H/PT-1H、before/after 墙钟 02:00→03:00、03:00→02:00）；isValidOffset 矩阵（gap 两个都 false，overlap 恰好两个 true）；compareTo 按 instant；equals/hashCode；越界工厂异常。

### 易错细节（JDK-8 实测锁定）
- overlap 的 `getDuration()` 是**负** PT-1H。
- `of(同偏移)` 抛 IAE `Offsets must not be equal`；`of(null,..)` NPE `transition`。
