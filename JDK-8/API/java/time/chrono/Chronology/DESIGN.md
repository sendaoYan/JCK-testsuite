# java.time.chrono.Chronology 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
日历体系接口：静态 `of(String)/ofLocale(Locale)/getAvailableChronologies`；`getId/getCalendarType/date.../localDateTime/zonedDateTime/eras/eraOf/isLeapYear/period/getDisplayName`。

## 2. Java 用户如何使用
```java
Chronology c = Chronology.ofLocale(Locale.forLanguageTag("th-TH-u-ca-buddhist"));
c.getId();  // ThaiBuddhist
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 注册表 5 项有序集合；of 大小写敏感映射 + `iso`（小写）抛 DTE `Unknown chronology: iso`；ofLocale 需要 **u-ca 关键字**或 ja-JP-JP variant 才选非 ISO（裸 th_TH 是 ISO）；eraOf 越界 DTE `Invalid era: N`；getId/getCalendarType 五套精确表。

### 易错细节（JDK-8 实测锁定）
- `of(null)` NPE `id`、`ofLocale(null)` NPE `locale`。
- 裸 `new Locale("th","TH")` 得到 **ISO**（COMPAT/CLDR 都不按地区猜日历）。
