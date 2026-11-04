# java.time.zone.ZoneOffsetTransitionRule 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
末位规则：`of(Month,int dom,DayOfWeek,LocalTime,boolean,TimeDefinition,ZoneOffset,ZoneOffset,ZoneOffset)`、`createTransition(int)`、全部 getter、`equals/hashCode/toString`。

## 2. Java 用户如何使用
```java
ZoneOffsetTransitionRule r = ZoneOffsetTransitionRule.of(
    Month.MARCH, 25, DayOfWeek.SUNDAY, LocalTime.of(2,0), false,
    TimeDefinition.WALL, ZoneOffset.ofHours(1), ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
r.createTransition(2024); // Berlin gap
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 构造柏林末位规则并逐年 createTransition（2024-03-31 / 2025-03-30 / overlap 10-27）；dom 正负语义（25=`on or after MARCH 25`、-5=`on or before last day minus 4`）；`null` DayOfWeek 变精确日规则；eod=true 的 24:00 打印与次日展开；越界 dom IAE；各 null NPE。

### 易错细节（JDK-8 实测锁定）
- dom 范围 IAE message：`Day of month indicator must be between -28 and 31 inclusive excluding zero`。
- `timeEndOfDay=true` 且非午夜抛 IAE `Time must be midnight when end of day flag is true`。
- **JDK-8 的 timeDefinition 参数 NPE message 有拼写错误 `timeDefnition`**，JDK-27 修正为 `timeDefinition`——按侧锁定。
- 偏移方向不做校验（同向/反向都合法，方向由 before/after 大小决定）。
