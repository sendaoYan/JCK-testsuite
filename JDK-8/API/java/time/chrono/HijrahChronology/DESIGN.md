# java.time.chrono.HijrahChronology 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
伊斯兰（Umm al-Qura）日历：id `Hijrah-umalqura`、calendarType `islamic-umalqura`；date/period/eraOf/isLeapYear/displayName。

## 2. Java 用户如何使用
```java
HijrahDate d = HijrahDate.of(1445, 12, 10);
d.lengthOfYear();  // 354
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- id/calendarType 精确串；年份范围 1300-1600；eraOf(1)=AH、eraOf(2) DTE `invalid Hijrah era`（小写开头，与 Japanese/ISO 的 `Invalid era` 不同）；dateEpochDay/dateYearDay/dateNow(fixed clock) 精确串；isLeapYear(1445)=false。

### 易错细节（JDK-8 实测锁定）
- **Umm al-Qura 数据窗口 1300-1600 AH**（约 1882-2174 CE），窗外 `Invalid Hijrah date, year: ...`。
- displayName FULL：JDK-8 `Islamic Umm al-Qura Calendar` vs JDK-27 `Hijri Calendar (Umm al-Qura)`——按侧锁定。
