# java.time.zone.ZoneRules 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
时区规则：`of(ZoneOffset)/of(5 参)`、`getOffset(Instant)/getValidOffsets/getTransition/getStandardOffset/isDaylightSavings/getDaylightSavings/isFixedOffset/nextTransition/previousTransition/getTransitions/getTransitionRules/equals/toString`。

## 2. Java 用户如何使用
```java
ZoneRules r = ZoneId.of("Europe/Berlin").getRules();
r.getOffset(Instant.parse("2024-07-15T12:00:00Z")); // +02:00
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- Berlin：1 月 +01:00/7 月 +02:00、stdOffset 恒 +01:00、DST 布尔与时长 PT1H；gap 区 validOffsets 空 + getTransition 非空、overlap 区两个 validOffsets `[+02:00, +01:00]` 且 getTransition 也**非空**、常态一个 offset 且 transition null；next/previousTransition 精确串；历史 transitions 计数 63（tzdata 39）、末位规则 2 条精确串；ZoneRules.of 固定偏移全表 + toString `ZoneRules[currentStandardOffset=+01:00]`。

### 易错细节（JDK-8 实测锁定）
- **overlap 里 getTransition 返回非 null 的 overlap 转换对象**（容易误以为只有 gap 才返回）。
- `ZoneRules.of(null)` 1 参 NPE message 是 `offset`，5 参才是 `baseStandardOffset`。
- transitions 计数依赖 tzdata 版本（两侧同为 39，锁定 63 条）。
