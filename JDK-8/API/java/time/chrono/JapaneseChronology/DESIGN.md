# java.time.chrono.JapaneseChronology 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
日本和历：id `Japanese`、calendarType `japanese`；eraOf/eras/isLeapYear/period/dateNow/displayName。

## 2. Java 用户如何使用
```java
JapaneseChronology.INSTANCE.eraOf(3); // Reiwa
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 五纪元有序（Meiji/Taisho/Showa/Heisei/Reiwa）；eraOf(-1..3) 全映射、eraOf(6/99) DTE `Invalid era: N`；dateNow(fixed)；YEAR_OF_ERA 范围随纪元（Showa `1 - 63`、Reiwa `1 - 292276976`）。

### 易错细节（JDK-8 实测锁定）
- Reiwa 在 JDK-8 也能用（eraOf(3)、valueOf("Reiwa")），只是 **`JapaneseEra.REIWA` 字段是 private**（JDK-27 是 public）——按侧锁定。
