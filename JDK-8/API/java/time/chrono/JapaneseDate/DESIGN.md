# java.time.chrono.JapaneseDate 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
和历日期：`of(int,int,int)`、`of(JapaneseEra,int,int,int)`、`from/atTime/plus/minus/with/until/format/getEra`。

## 2. Java 用户如何使用
```java
JapaneseDate d = JapaneseDate.of(2024, 6, 15);
d.toString(); // Japanese Reiwa 6-06-15
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 工厂三形态（ISO 年月日 / 纪元年月日 / from(LocalDate)）；纪元边界（Reiwa 首日 2019-05-01、Showa 首日 1926-12-25、最小支持 Meiji 6 = 1873-01-01）；era 越界（Showa 64 / Meiji 45）DTE `year, month, and day not valid for Era`；minus(10 年) 跨纪元到 Heisei 26；YEAR_OF_ERA/ERA 字段值；与 LocalDate 的 equals=false/compareTo/isEqual。

### 易错细节（JDK-8 实测锁定）
- **1873-01-01 之前不支持**：`JapaneseDate before Meiji 6 is not supported`（不是普通日期范围错误）。
- Showa 元年 12 月 25 日前（1926-12-25 之前的 Showa 1）非法。
- equals 跨日历恒 false，但 isEqual 按时间线 true。
