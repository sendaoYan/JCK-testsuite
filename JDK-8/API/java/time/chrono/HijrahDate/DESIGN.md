# java.time.chrono.HijrahDate 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
伊斯兰历日期：`of(int,int,int)`、`from/parse/atTime/plus/minus/with/until/range/lengthOfMonth/lengthOfYear/toEpochDay/isLeapYear/format`。

## 2. Java 用户如何使用
```java
HijrahDate d = HijrahDate.of(1445, 12, 10);
LocalDate iso = LocalDate.from(d); // 2024-06-16
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 固定日期全字段（lom 30/loy 354/DOY 334/ranges `1 - 30`、`1 - 12`、`1300 - 1600`）；ISO 往返（1445-12-10 ↔ 2024-06-16、2024-06-15 ↔ 1445-12-09）；plus 天/月；firstDayOfNextMonth；窗口边界 1300-01-01/1600-12-30 与越界 message；toString 精确 `Hijrah-umalqura AH 1445-12-10`。

### 易错细节（JDK-8 实测锁定）
- **往 ISO 换与从 ISO 来不是同一对日期**（1445-12-10→2024-06-16 而 2024-06-15→1445-12-09）：往返必须各测各向。
- 月 13 `Invalid Hijrah month: 13`；日 31 `Invalid Hijrah day of month: 31`；窗外年 `Invalid Hijrah date, year: 1299, month: 1`。
