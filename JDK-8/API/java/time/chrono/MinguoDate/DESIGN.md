# java.time.chrono.MinguoDate 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
民国历日期：`of(int,int,int)`、`from/atTime/plus/until/with/format/getEra`。

## 2. Java 用户如何使用
```java
MinguoDate d = MinguoDate.of(113, 6, 15);
LocalDate.from(d); // 2024-06-15
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 1911 年偏移双向换算（113↔2024）；ROC 元年 1912-01-01、之前是 BEFORE_ROC（1911-12-31 = BEFORE_ROC 1-12-31）；year 0/-1 的 BEFORE_ROC 递增语义；plus/until/format('G')；非法日 DTE `Invalid date 'FEBRUARY 30'`。

### 易错细节（JDK-8 实测锁定）
- **year 0 是 BEFORE_ROC 1**（不是 ROC 0）；-1 是 BEFORE_ROC 2。
- format 'G'：JDK-8 `R.O.C.` vs JDK-27 `Minguo`——按侧锁定。
