# java.time.chrono.ThaiBuddhistDate 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
佛历日期：`of(int,int,int)`、`from/with/until/format/getEra`。

## 2. Java 用户如何使用
```java
ThaiBuddhistDate d = ThaiBuddhistDate.of(2567, 6, 15);
LocalDate.from(d); // 2024-06-15
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 543 偏移双向换算；year 0/-1 = BEFORE_BE 1/2；with(YEAR)；until 年差；format('G')；epochDay 与 ISO 同值。

### 易错细节（JDK-8 实测锁定）
- format 'G'：JDK-8 `B.E.` vs JDK-27 `BE`——按侧锁定。
- BEFORE_BE 的年份随负年递增（同 Minguo 规则）。
