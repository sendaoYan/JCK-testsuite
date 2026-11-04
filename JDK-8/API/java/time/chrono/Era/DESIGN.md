# java.time.chrono.Era 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
纪元接口：`getValue/getDisplayName(TemporalStyle,Locale)`、继承 TemporalAccessor 的 `range/get/query`；实现类 JapaneseEra/MinguoEra/ThaiBuddhistEra/IsoEra/HijrahEra。

## 2. Java 用户如何使用
```java
Era e = JapaneseDate.of(2024,6,15).getEra();
e.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 各日历 eras() 有序表；getValue 精确映射（JapaneseEra -1..3）；getEra 往返；query(precision) 返回 `Eras`；range(YEAR_OF_ERA) 一律 UTE `Unsupported field: YearOfEra`。

### 易错细节（JDK-8 实测锁定）
- **Era 接口没有 range 支持**：所有纪元的 `range(YEAR_OF_ERA)` 抛 UTE（想拿年份范围要问 `JapaneseDate.range`）。
- `HijrahChronology` 只有一个纪元 AH。
